package org.hl7.davinci.stu3;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.hl7.fhir.dstu3.model.Resource;

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
