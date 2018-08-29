package org.hl7.davinci.cdshooks;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import javafx.util.Pair;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public abstract class AbstractFetcher {

  static final Logger logger = LoggerFactory.getLogger(AbstractFetcher.class);

  protected CrdPrefetch prefetch;
  protected LinkedHashMap<String,String> oauth;
  protected String fhirServer;

  protected Map<Pair<ResourceType, String>, Resource> resources;


  /**
   * Constructor called by subclasses that sets member variables based on CdsRequest provided.
   * @param request contains data needed by the fetcher
   */
  public AbstractFetcher(CdsRequest request) {
//    this.prefetch = request.getPrefetch();
    this.oauth = (LinkedHashMap) request.getOauth();
    this.fhirServer = request.getFhirServer();

    resources = new HashMap<Pair<ResourceType, String>, Resource>();
  }

  /**
   * Get the resource matching the key values.
   * @param type is the type of the resource
   * @param reference is the reference of the resource
   * @return the indexed Resource
   */
  public final Resource getResource(ResourceType type, String reference) {
    Pair<ResourceType, String> key = new Pair<>(type, reference);
    return resources.get(key);
  }

  /**
   * Fetches the remaining resources referenced by the context that are not found in the prefetch.
   */
  public abstract void fetch();

  /**
   * Checks if a valid request was provided in the context.
   * @return true if a request was provided, false otherwise.
   */
  public abstract boolean hasRequest();

  protected final IGenericClient composeClient(String server, LinkedHashMap<String,String> oauth) {
    FhirContext ctx = FhirContext.forR4();
    BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(oauth.get("access_token"));

    IGenericClient client = ctx.newRestfulGenericClient(server);
    client.registerInterceptor(authInterceptor);
    return client;
  }

}
