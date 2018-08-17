package org.hl7.davinci;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;

import java.io.IOException;

public class JacksonBundleDeserializer extends StdDeserializer<Bundle> {
  private static FhirContext ctx = FhirContext.forR4();

  public JacksonBundleDeserializer() {
    this(Bundle.class);
  }

  public JacksonBundleDeserializer(Class<Bundle> vc) {
    super(vc);
  }

  @Override
  public Bundle deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode node = mapper.readTree(p);
    IBaseResource parsedResource = ctx.newJsonParser().parseResource(mapper.writeValueAsString(node));
    Bundle b = (Bundle) parsedResource;
    return b;
  }
}
