package org.hl7.davinci.r4;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.apache.commons.beanutils.PropertyUtils;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.fhir.r4.model.Bundle;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class JacksonPrefetchDeserializer extends StdDeserializer<CrdPrefetch> {

  public JacksonPrefetchDeserializer() {
    this(CrdPrefetch.class);
  }

  protected JacksonPrefetchDeserializer(Class<CrdPrefetch> vc) {
    super(vc);
  }

  @Override
  public CrdPrefetch deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode prefetchArray = mapper.readTree(p);
    if(!prefetchArray.isArray()){
      throw new RuntimeException("Input prefetch must be in array form.");
    }
    CrdPrefetch crdPrefetch = new CrdPrefetch();
    FhirComponents fhirComponents = new FhirComponents();
    for(final JsonNode currentPrefetch : prefetchArray){
      System.out.println("CURRENT PREF" + currentPrefetch);
      String prefetchKey = currentPrefetch.fieldNames().next();
      JsonNode prefetchValue = currentPrefetch.get(prefetchKey);
      Bundle parsedResource = (Bundle) fhirComponents.getJsonParser().parseResource(mapper.writeValueAsString(prefetchValue));
      // FhirRequestProcessor.addToCrdPrefetchRequest(crdPrefetch, parsedResource.getEntryFirstRep().getResource().getResourceType(), parsedResource);
      System.out.println("PARSED PREF" + parsedResource);
      try {
        System.out.println("1" + parsedResource);
        PropertyUtils.setProperty(crdPrefetch, prefetchKey, parsedResource);
        System.out.println("2" + parsedResource);
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        System.out.println("3" + parsedResource);
        e.printStackTrace();
      }
      System.out.println("4" + parsedResource);
    }
    System.out.println("DESERIALIZED: " + crdPrefetch);
    return crdPrefetch;
  }
  
}
