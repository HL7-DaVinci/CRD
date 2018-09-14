package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import org.cdshooks.CdsResponse;
import org.hl7.davinci.r4.CrdRequestCreator;
import org.hl7.davinci.r4.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MedicationPrescribeServiceTest {

  @Autowired
  private MedicationPrescribeService service;

  @Test
  public void testHandleRequest() {
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    MedicationPrescribeRequest request = CrdRequestCreator
        .createMedicationPrescribeRequest(Enumerations.AdministrativeGender.MALE, cal.getTime());
    CdsResponse response = service.handleRequest(request);
    assertNotNull(response);
    assertEquals(1, response.getCards().size());
    assertEquals("Documentation is required for the desired device or service", response.getCards().get(0).getSummary());
  }
}
