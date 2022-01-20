package org.cdshooks;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ActionSerializer extends StdSerializer<Action> {

  public ActionSerializer() {
    this(null);
  }

  public ActionSerializer(Class<Action> t) {
    super(t);
  }

  @Override
  public void serialize(
      Action value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {

    jgen.writeStartObject();
    jgen.writeStringField("type", value.getType().toString());
    jgen.writeStringField("description", value.getDescription());

    // convert the IBaseResource to a string
    String jsonStrNew = value.getFhirComponents().getFhirContext().newJsonParser()
        .encodeResourceToString(value.getResource());

    // parse it back into a Map
    Map<String,Object> map =
        new ObjectMapper().readValue(jsonStrNew, HashMap.class);
    jgen.writeObjectField("resource", map);

    jgen.writeStringField("resourceId", value.getResourceId());

    jgen.writeEndObject();
  }
}
