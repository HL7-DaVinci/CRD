package org.hl7.davinci.cdshooks.orderreview;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class CrdCdsRequestTest {
  @Test
  public void testReadingJson() throws IOException, FHIRException {
    InputStream requestStream = this.getClass().getResourceAsStream("request.json");
    ObjectMapper mapper = new ObjectMapper();
    CrdCdsRequest request = mapper.readValue(requestStream, CrdCdsRequest.class);
    assertNotNull(request);
    assertEquals("1288992", request.getContext().getPatientId());
    assertEquals("male", request.getPrefetch().getPatient().getGender().toCode());
    DeviceRequest dr = (DeviceRequest) request.getContext().getOrders().getEntry().get(0).getResource();
    assertNotNull(dr);
    assertEquals("E0424", dr.getCodeCodeableConcept().getCoding().get(0).getCode());
  }
}
