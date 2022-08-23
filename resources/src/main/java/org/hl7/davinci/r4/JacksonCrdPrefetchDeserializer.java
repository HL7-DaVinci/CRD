package org.hl7.davinci.r4;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.fhir.r4.model.Bundle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class JacksonCrdPrefetchDeserializer extends StdDeserializer<CrdPrefetch> {

  public JacksonCrdPrefetchDeserializer() {
    this(CrdPrefetch.class);
  }

  public JacksonCrdPrefetchDeserializer(Class<CrdPrefetch> vc) {
    super(vc);
  }

  @Override
  public CrdPrefetch deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {  
    CrdPrefetch prefetch = new CrdPrefetch();  
    Map<String, Consumer<Bundle>> deserializeMap = new HashMap<>();
    deserializeMap.put("coverageBundle", (bundle) -> prefetch.setCoverageBundle(bundle));
    deserializeMap.put("deviceRequestBundle", (bundle) -> prefetch.setDeviceRequestBundle(bundle));
    deserializeMap.put("medicationRequestBundle", (bundle) -> prefetch.setMedicationRequestBundle(bundle));
    deserializeMap.put("nutritionOrderBundle", (bundle) -> prefetch.setNutritionOrderBundle(bundle));
    deserializeMap.put("serviceRequestBundle", (bundle) -> prefetch.setServiceRequestBundle(bundle));
    deserializeMap.put("supplyRequestBundle", (bundle) -> prefetch.setSupplyRequestBundle(bundle));
    deserializeMap.put("appointmentBundle", (bundle) -> prefetch.setAppointmentBundle(bundle));
    deserializeMap.put("encounterBundle", (bundle) -> prefetch.setEncounterBundle(bundle));
    deserializeMap.put("medicationDispenseBundle", (bundle) -> prefetch.setMedicationDispenseBundle(bundle));
    deserializeMap.put("medicationStatementBundle", (bundle) -> prefetch.setMedicationStatementBundle(bundle));

    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode prefetchNode = mapper.readTree(p);
    FhirComponents fhirComponents = new FhirComponents();

    Iterator<Entry<String, JsonNode>> prefetchFields = prefetchNode.fields();
    while(prefetchFields.hasNext()) {
      Entry<String, JsonNode> prefetchPair = prefetchFields.next();
      String prefetchKey = prefetchPair.getKey();
      JsonNode prefetchVal = prefetchPair.getValue();
      System.out.println("l:"+mapper.writeValueAsString(prefetchVal));

      Bundle prefetchBundle = (Bundle) fhirComponents.getJsonParser().parseResource(mapper.writeValueAsString(prefetchVal));
      // Map the bundle to the prefetch.
      deserializeMap.get(prefetchKey).accept(prefetchBundle);
    }

    System.out.println("Parsed Prefetch: " + prefetch);

    return prefetch;
  }
}
