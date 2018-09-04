package org.hl7.davinci.endpoint.components.prefetchHydrator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.davinci.cdshooks.CdsRequest;
import org.hl7.davinci.cdshooks.CrdPrefetch;
import org.hl7.davinci.cdshooks.CdsService;
import org.hl7.davinci.endpoint.components.FhirComponents;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class PrefetchHydrator {

  private static String PREFETCH_TOKEN_DELIM_OPEN = "{{";
  private static String PREFETCH_TOKEN_DELIM_CLOSE = "}}";

  private CdsService cdsService;
  private CdsRequest cdsRequest;
  private Object dataForPrefetchToken;
  private FhirComponents fhirComponents;

  public PrefetchHydrator(CdsService cdsService, CdsRequest cdsRequest,
      FhirComponents fhirComponents) {
    this.cdsService = cdsService;
    this.cdsRequest = cdsRequest;
    this.dataForPrefetchToken = cdsRequest.getDataForPrefetchToken();
    this.fhirComponents = fhirComponents;
  }

  public void hydrate(){
    CrdPrefetch crdResponse = cdsRequest.getPrefetch();
    for (String prefetchKey: cdsService.prefetch.keySet()){
      //check if the
      Boolean alreadyIncluded = false;
      try {
        alreadyIncluded = (PropertyUtils.getProperty(crdResponse,prefetchKey) != null);
      } catch (Exception e) {
        throw new java.lang.RuntimeException("System error: Mismatch in prefetch keys between the "
            + "CrdPrefetch and the key templates set in the service.");
      }
      if (!alreadyIncluded){
        // check if the bundle actually has element
        String prefetchQuery = cdsService.prefetch.get(prefetchKey);
        String hydratedPrefetchQuery = hydratePrefetchQuery(prefetchQuery);
        // if we can't hydrate the query, it probably means we didnt get an apprpriate resource
        // e.g. this could be a query template for a medication order but we have a device request
        if (hydratedPrefetchQuery != null) {
          try {
            PropertyUtils.setProperty(crdResponse,prefetchKey,executeFhirQuery(hydratedPrefetchQuery));
          } catch (Exception e) {
            System.out.println("Failed to fill prefetch for key: "+prefetchKey);
            //TODO: log?
          }
        }
      }
    }
  }

  private Bundle executeFhirQuery(String query) {
    String fullUrl = cdsRequest.getFhirServer() + query;
//    IGenericClient client = ctx.newRestfulGenericClient(serverBase);
//    return client.search().byUrl(query).encodedJson().returnBundle(Bundle.class).execute();
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<String> entity = new HttpEntity<>("", headers);
    try {
      ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET,
          entity, String.class);
      return (Bundle) fhirComponents.getJsonParser().parseResource(response.getBody());
    } catch (Exception e) {
      return null;
    }

  }

  private String hydratePrefetchQuery(String prefetchQuery) {
    String[] tokenList = StringUtils.substringsBetween(
        prefetchQuery, PREFETCH_TOKEN_DELIM_OPEN, PREFETCH_TOKEN_DELIM_CLOSE);
    for (String token: tokenList) {
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
    List<String> elementList = new ArrayList<>();
    List<String> pathList = Arrays.asList(prefetchToken.split("\\."));
    resolvePrefetchTokenRecursive(dataForPrefetchToken, pathList, elementList);
    return String.join(",",elementList);
  }

  private static void resolvePrefetchTokenRecursive(
      Object object, List<String> pathList, List<String> elementList) {
    if (pathList.size() == 0) {
      elementList.add(object.toString());
      return;
    }

    try {
      //special logic for "id" since hapi puts the unqualified id part kind of deep
      if (pathList.get(0).equals("id")) {
        object = ((IBaseResource) object).getIdElement().getIdPart();
      } else {
        object = PropertyUtils.getProperty(object, pathList.get(0));
      }
    } catch(Exception e) {
      return;
    }
    List<String> remaingPathList = pathList.subList(1,pathList.size());

    if (object instanceof List){
      for (Object entry: (List) object) {
        resolvePrefetchTokenRecursive(entry, remaingPathList, elementList);
      }
      return;
    }

    resolvePrefetchTokenRecursive(object, remaingPathList, elementList);
  }
}
