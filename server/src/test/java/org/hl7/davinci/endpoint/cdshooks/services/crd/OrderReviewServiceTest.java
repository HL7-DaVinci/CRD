package org.hl7.davinci.endpoint.cdshooks.services.crd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Calendar;

import org.hl7.davinci.CrdRequestCreator;
import org.hl7.davinci.cdshooks.CdsResponse;
import org.hl7.davinci.cdshooks.orderreview.OrderReviewRequest;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderReviewServiceTest {

  @Autowired
  private OrderReviewService service;

  @Test
  public void testHandleRequest() {
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    OrderReviewRequest request = CrdRequestCreator.createRequest(Enumerations.AdministrativeGender.MALE, cal.getTime());
    CdsResponse response = service.handleRequest(request);
    assertNotNull(response);
    assertEquals(1, response.getCards().size());
    assertEquals("No documentation rules found", response.getCards().get(0).getSummary());
  }
}
