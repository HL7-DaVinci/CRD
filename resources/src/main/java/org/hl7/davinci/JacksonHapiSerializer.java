package org.hl7.davinci;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.hl7.fhir.r4.model.Resource;

import java.io.IOException;

public class JacksonHapiSerializer extends StdSerializer<Resource> {
  private static FhirContext ctx = FhirContext.forR4();

  public JacksonHapiSerializer() {
    this(null);
  }

  public JacksonHapiSerializer(Class<Resource> r) {
    super(r);
  }

  @Override
  public void serialize(Resource value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    String resourceString = ctx.newJsonParser().encodeResourceToString(value);
    gen.writeRawValue(resourceString);
  }
}
