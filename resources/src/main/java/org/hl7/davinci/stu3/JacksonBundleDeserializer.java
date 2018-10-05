package org.hl7.davinci.stu3;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.dstu3.model.Bundle;

public class JacksonBundleDeserializer extends StdDeserializer<Bundle> {

  public JacksonBundleDeserializer() {
    this(Bundle.class);
  }

  public JacksonBundleDeserializer(Class<Bundle> vc) {
    super(vc);
  }

  @Override
  public Bundle deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    FhirComponents fhirComponents = new FhirComponents();
    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode node = mapper.readTree(p);
    IBaseResource parsedResource = fhirComponents.getJsonParser().parseResource(mapper.writeValueAsString(node));
    return (Bundle) parsedResource;
  }
}
