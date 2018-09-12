package org.hl7.davinci.stu3.crdhook.medicationprescribe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.hl7.davinci.stu3.Utilities;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.jupiter.api.Test;

public class MedicationPrescribeRequestTest {
  @Test
  public void testReadingJson() throws IOException, FHIRException {
    InputStream requestStream = this.getClass().getResourceAsStream("requestWithHydratedPrefetchBundle.json");
    ObjectMapper mapper = new ObjectMapper();
    MedicationPrescribeRequest request = mapper.readValue(requestStream, MedicationPrescribeRequest.class);
    assertNotNull(request);
    assertEquals("1288992", request.getContext().getPatientId());

    Bundle medicationRequestBundle = request.getPrefetch().getMedicationRequestBundle();
    List<MedicationRequest> medicationRequestList = Utilities.getResourcesOfTypeFromBundle(
        MedicationRequest.class, medicationRequestBundle);

    MedicationRequest medicationRequest = medicationRequestList.get(0);
    Patient patient = (Patient) medicationRequest.getSubject().getResource();

    assertNotNull(medicationRequest);
    assertEquals("314076", medicationRequest.getMedicationCodeableConcept().getCoding().get(0).getCode());
    assertEquals("male", patient.getGender().toCode());
  }
}
