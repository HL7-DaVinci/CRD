package org.hl7.davinci.r4;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.hl7.fhir.instance.model.Bundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.OperationOutcome;

import java.io.IOException;

public class JacksonIBaseResourceDeserializer extends StdDeserializer<IBaseResource> {

  public JacksonIBaseResourceDeserializer() {
    this(null);
  }

  public JacksonIBaseResourceDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public IBaseResource deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    FhirComponents fhirComponents = new FhirComponents();
    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode node = mapper.readTree(p);
    IBaseResource parsedResource = fhirComponents.getJsonParser().parseResource(mapper.writeValueAsString(node));
    return parsedResource;
  }
}
