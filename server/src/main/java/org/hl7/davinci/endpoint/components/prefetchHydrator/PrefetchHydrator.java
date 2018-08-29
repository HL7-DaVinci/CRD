package org.hl7.davinci.endpoint.components.prefetchHydrator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.davinci.cdshooks.CdsRequest;
import org.hl7.davinci.cdshooks.PrefetchResponse;
import org.hl7.davinci.cdshooks.CdsService;
import org.hl7.fhir.r4.model.Bundle;

public class PrefetchHydrator {

  private static String PREFETCH_TOKEN_DELIM_OPEN = "{{";
  private static String PREFETCH_TOKEN_DELIM_CLOSE = "}}";

  private CdsService cdsService;
  private CdsRequest cdsRequest;
  private Object dataForPrefetchToken;

  public PrefetchHydrator(CdsService cdsService, CdsRequest cdsRequest) {
    this.cdsService = cdsService;
    this.cdsRequest = cdsRequest;
    this.dataForPrefetchToken = cdsRequest.getDataForPrefetchToken();
  }

  public void hydrate(){
    PrefetchResponse prefetchResponse = cdsRequest.getPrefetch();
    for (String prefetchKey: cdsService.prefetch.keySet()){
      if (!prefetchResponse.containsKey(prefetchKey)){
        // check if the bundle actually has element
        String prefetchQuery = cdsService.prefetch.get(prefetchKey);
        String hydratedPrefetchQuery = hydratePrefetchQuery(prefetchQuery);
        // if we can't hydrate the query, it probably means we didnt get an apprpriate resource
        // e.g. this could be a query template for a different order type than the one we got
        if (hydratedPrefetchQuery != null) {
          try {
            prefetchResponse.put(prefetchKey, executeFhirQuery(hydratedPrefetchQuery));
          } catch (Exception e) {
            //TODO: log?
          }
        }
      }
    }
  }

  private Bundle executeFhirQuery(String query) {
    FhirContext ctx = FhirContext.forR4();
    String serverBase = cdsRequest.getFhirServer();
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);

    return client.search().byUrl(query).returnBundle(Bundle.class).execute();
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
      object = PropertyUtils.getProperty(object, pathList.get(0));
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
