package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.cdshooks.CdsResponse;
import org.hl7.davinci.r4.CrdRequestCreator;
import org.hl7.davinci.r4.crdhook.ordersign.OrderSignRequest;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderSignServiceTest {

  static final Logger logger = LoggerFactory.getLogger(OrderSignServiceTest.class);
  @Autowired
  private OrderSignService service;

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
    OrderSignRequest request = CrdRequestCreator
        .createOrderSignRequest(Enumerations.AdministrativeGender.MALE, cal.getTime(), "MA", "MA");
    CdsResponse response = service.handleRequest(request, applicationBase);
    assertNotNull(response);
    assertEquals(1, response.getCards().size());
    String summary = response.getCards().get(0).getSummary();
    assertTrue(summary.endsWith("Prior Authorization required."));
  }
}
