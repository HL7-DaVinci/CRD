package org.hl7.davinci.r4;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.hl7.fhir.r4.model.Resource;

import java.io.IOException;

public class JacksonHapiSerializer extends StdSerializer<Resource> {

  public JacksonHapiSerializer() {
    this(null);
  }

  public JacksonHapiSerializer(Class<Resource> r) {
    super(r);
  }

  @Override
  public void serialize(Resource value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    FhirComponents fhirComponents = new FhirComponents();
    String resourceString = fhirComponents.getJsonParser().encodeResourceToString(value);
    gen.writeRawValue(resourceString);
  }
}
