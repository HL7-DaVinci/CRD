package org.hl7.davinci.cdshooks.medicationprescribe;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.davinci.cdshooks.orderreview.OrderReviewRequest;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MedicationPrescribeRequestTest {
  @Test
  public void testReadingJson() throws IOException, FHIRException {
    InputStream requestStream = this.getClass().getResourceAsStream("request.json");
    ObjectMapper mapper = new ObjectMapper();
    MedicationPrescribeRequest request = mapper.readValue(requestStream, MedicationPrescribeRequest.class);
    assertNotNull(request);
    assertEquals("1288992", request.getContext().getPatientId());
    assertEquals("male", request.getPrefetch().getPatient().getGender().toCode());
    MedicationRequest mr = (MedicationRequest) request.getContext().getMedications().getEntry().get(0).getResource();
    assertNotNull(mr);
    assertEquals("314076", mr.getMedicationCodeableConcept().getCoding().get(0).getCode());
  }
}
