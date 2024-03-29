package org.hl7.davinci.endpoint.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.cdshooks.CdsRequest;
import org.hl7.davinci.FatalRequestIncompleteException;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.FhirRequestProcessor;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

public class PrefetchHydrator {

  static final Logger logger =
      LoggerFactory.getLogger(PrefetchHydrator.class);

  private static final String PREFETCH_TOKEN_DELIM_OPEN = "{{";
  private static final String PREFETCH_TOKEN_DELIM_CLOSE = "}}";

  private CdsService<?> cdsService;
  private CdsRequest<?, ?> cdsRequest;
  private Object dataForPrefetchToken;
  private FhirComponentsT fhirComponents;

  private String currentlyResolvingPrefetchToken;

  /**
   * Constructor should take in a service and a request that service is processing. This class can
   * fill out the prefetch elements that are missing.
   *
   * @param cdsService The service that is processing the request.
   * @param cdsRequest The request in question, the prefetch will be hydrated if possible. Note that
   *                   this object gets modified.
   * @param fhirComponents The fhir components object.
   */
  public PrefetchHydrator(CdsService cdsService, CdsRequest cdsRequest,
      FhirComponentsT fhirComponents) {
    this.cdsService = cdsService;
    this.cdsRequest = cdsRequest;
    this.dataForPrefetchToken = cdsRequest.getDataForPrefetchToken();
    this.fhirComponents = fhirComponents;
  }

  private void resolvePrefetchTokenRecursive(
      Object object, List<String> pathList, List<String> elementList) {
    if (object == null) {
      return;
    }
    if (pathList.size() == 0) {
      elementList.add(object.toString());
      return;
    }


    // if a resource exists but has no id, throw an error rather than continuing
    if (pathList.get(0).equals("id")) {
      try {
        //special logic for "id" since hapi puts the unqualified id part kind of deep
        object = ((IBaseResource) object).getIdElement().getIdPart();
      } catch (Exception e) {
        return;
      }
      if (object == null) {
        throw new FatalRequestIncompleteException("While attempting to resolve prefetch "
            + "token '" + currentlyResolvingPrefetchToken + "', a resource was found without an ID.");
      }
    } else {
      try {
        object = PropertyUtils.getProperty(object, pathList.get(0));
      } catch (Exception e) {
        return;
      }
    }
    List<String> remaingPathList = pathList.subList(1, pathList.size());

    if (object instanceof List) {
      for (Object entry : (List) object) {
        resolvePrefetchTokenRecursive(entry, remaingPathList, elementList);
      }
      return;
    }

    resolvePrefetchTokenRecursive(object, remaingPathList, elementList);
  }

  /**
   * Attempt to hydrate missing prefetch elements, note that this modifies the request object.
   */
  public void hydrate() {
    Object crdResponse = cdsRequest.getPrefetch();
    for (PrefetchTemplateElement prefetchElement : cdsService.getPrefetchElements()) {
      String prefetchKey = prefetchElement.getKey();
      IBaseResource resource;
      //check if the prefetch has already been populated with that key
      Boolean alreadyIncluded = false;
      try {
        resource = (IBaseResource) PropertyUtils.getProperty(crdResponse, prefetchKey);

        if (resource != null && resource.getClass() != OperationOutcome.class) {
          alreadyIncluded = true;
        }
      } catch (Exception e) {
        throw new RuntimeException("System error: Mismatch in prefetch keys between the "
            + "CrdPrefetch and the key templates set in the service. Given prefetch key '" + prefetchKey + "''.", e);
      }

      if (!alreadyIncluded) {
        // check if the bundle actually has element
        String prefetchQuery = cdsService.prefetch.get(prefetchKey);
        String hydratedPrefetchQuery = hydratePrefetchQuery(prefetchQuery);
        // if we can't hydrate the query, it probably means we didn't get an appropriate resource
        // e.g. this could be a query template for a medication order, but we have a device request
        if (hydratedPrefetchQuery != null) {
          if (cdsRequest.getFhirServer() == null) {
            throw new FatalRequestIncompleteException("Attempting to fill the prefetch, but no fhir "
                + "server provided. Either provide a full prefetch or provide a fhir server.");
          }

          if (resource == null) {
            try {
              PropertyUtils
                .setProperty(crdResponse, prefetchKey,
                    prefetchElement.getReturnType().cast(
                        FhirRequestProcessor.executeFhirQueryUrl(hydratedPrefetchQuery, cdsRequest, fhirComponents, HttpMethod.GET)));
            } catch (Exception e) {
              logger.warn("Failed to fill prefetch for key: " + prefetchKey, e);
            }
            continue;
          }

          Bundle newBundle = (Bundle) prefetchElement.getReturnType().cast(FhirRequestProcessor.executeFhirQueryUrl(hydratedPrefetchQuery, cdsRequest, fhirComponents, HttpMethod.GET));

          if (newBundle == null && resource.getClass() == OperationOutcome.class) {
            //Return Code 412 - Precondition Failed: Specified in https://cds-hooks.org/specification/current/#prefetch-template
            OperationOutcome.OperationOutcomeIssueComponent firstOutcome = ((OperationOutcome.OperationOutcomeIssueComponent) ((OperationOutcome) resource).getIssue().toArray()[0]);

            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Failed to fill prefetch for key: " + prefetchKey + ". Prefetch Operation Outcome: " + firstOutcome.getSeverity().getDisplay() + " - " + firstOutcome.getDetails().getText());
          }

          try {
            PropertyUtils.setProperty(crdResponse, prefetchKey, newBundle);
          }
          catch (Exception e) {
            logger.warn("Failed to fill prefetch for key: " + prefetchKey, e);
          }
        }
      }
    }
  }

  private String hydratePrefetchQuery(String prefetchQuery) {
    String[] tokenList = StringUtils.substringsBetween(
        prefetchQuery, PREFETCH_TOKEN_DELIM_OPEN, PREFETCH_TOKEN_DELIM_CLOSE);
    for (String token : tokenList) {
      String resolvedToken = resolvePrefetchToken(token);
      if (resolvedToken.isEmpty()) {
        return null;
      }
      prefetchQuery = prefetchQuery.replaceAll(Pattern.quote(
          PREFETCH_TOKEN_DELIM_OPEN + token + PREFETCH_TOKEN_DELIM_CLOSE), resolvedToken);
    }
    return prefetchQuery;
  }

  private String resolvePrefetchToken(String prefetchToken) {
    currentlyResolvingPrefetchToken = prefetchToken;
    List<String> elementList = new ArrayList<>();
    List<String> pathList = Arrays.asList(prefetchToken.split("\\."));
    resolvePrefetchTokenRecursive(dataForPrefetchToken, pathList, elementList);
    return String.join(",", elementList);
  }
}