package org.hl7.davinci.endpoint.cdshooks.services.crd.stu3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import org.cdshooks.CdsResponse;
import org.hl7.davinci.stu3.CrdRequestCreator;
import org.hl7.davinci.stu3.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MedicationPrescribeServiceTest {

  @Autowired
  private MedicationPrescribeService service;

  @Test
  public void testHandleRequest() {
    URL applicationBase;
    try {
      applicationBase = new URL("http","localhost","/");
    } catch (MalformedURLException e){
      throw new RuntimeException(e);
    }
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    MedicationPrescribeRequest request = CrdRequestCreator
        .createMedicationPrescribeRequest(Enumerations.AdministrativeGender.MALE, cal.getTime(), "MA", "MA");
    CdsResponse response = service.handleRequest(request, applicationBase);
    assertNotNull(response);
    assertEquals(1, response.getCards().size());
    assertEquals("Authorization is required.", response.getCards().get(0).getSummary());
  }
}
