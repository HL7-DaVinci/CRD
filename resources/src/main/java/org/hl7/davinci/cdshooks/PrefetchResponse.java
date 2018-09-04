package org.hl7.davinci.cdshooks;

import java.util.HashMap;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * This could be used in a more generic setting but is not needed since we use CrdPrefetch
 */
public class PrefetchResponse extends HashMap<String, IBaseResource> { }

//  COMMENT FOR HOW YOU COULD BUILD THIS KIND OF GENERIC PREFETCH
//  private PrefetchResponse prefetch;
//
//  @JsonSetter("prefetch")
//  public void setPrefetch(JsonNode prefetchNode) {
//    prefetch = new PrefetchResponse();
//    FhirContext ctxR4 = FhirContext.forR4();
//    IParser parser = ctxR4.newJsonParser();
//
//    Iterator<String> keyIterator = prefetchNode.fieldNames();
//    while (keyIterator.hasNext()) {
//      String prefetchKey = keyIterator.next();
//      String fhirResourceAsJson = prefetchNode.get(prefetchKey).toString();
//      IBaseResource baseResource = parser.parseResource(fhirResourceAsJson);
//      prefetch.put(prefetchKey, baseResource);
//    }
//  }