package org.hl7.davinci.r4.crdhook.ordersign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

import java.util.List;
import org.hl7.davinci.r4.Utilities;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

public class OrderSignRequestTest {
  @Test
  public void testReadingJson() throws IOException, FHIRException {

    InputStream requestStream = this.getClass().getResourceAsStream("requestWithHydratedPrefetchBundle.json");
    ObjectMapper mapper = new ObjectMapper();
    OrderSignRequest request = mapper.readValue(requestStream, OrderSignRequest.class);
    assertNotNull(request);
    assertEquals("1288992", request.getContext().getPatientId());

    Bundle deviceRequestBundle = request.getPrefetch().getDeviceRequestBundle();
    List<DeviceRequest> deviceRequestList = Utilities.getResourcesOfTypeFromBundle(
        DeviceRequest.class, deviceRequestBundle);

    DeviceRequest deviceRequest = deviceRequestList.get(0);
    Patient patient = (Patient) deviceRequest.getSubject().getResource();

    assertNotNull(deviceRequest);
    assertEquals("E0424", deviceRequest.getCodeCodeableConcept().getCoding().get(0).getCode());
    assertEquals("male", (patient.getGender().toCode()));

  }
}
