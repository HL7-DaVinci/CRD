package org.hl7.davinci.cdshooks.orderreview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.junit.jupiter.api.Test;

public class OrderReviewRequestTest {
  @Test
  public void testReadingJson() throws IOException, FHIRException {
    InputStream requestStream = this.getClass().getResourceAsStream("request.json");
    ObjectMapper mapper = new ObjectMapper();
    OrderReviewRequest request = mapper.readValue(requestStream, OrderReviewRequest.class);
    assertNotNull(request);
    assertEquals("1288992", request.getContext().getPatientId());
    assertEquals("male", request.getPrefetch().getPatient().getGender().toCode());
    DeviceRequest dr = (DeviceRequest) request.getContext().getOrders().getEntry().get(0).getResource();
    assertNotNull(dr);
    assertEquals("E0424", dr.getCodeCodeableConcept().getCoding().get(0).getCode());
  }
}
