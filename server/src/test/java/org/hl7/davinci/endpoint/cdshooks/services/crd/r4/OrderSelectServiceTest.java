package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.cdshooks.Card;
import org.cdshooks.CdsResponse;
import org.hl7.davinci.r4.CrdRequestCreator;
import org.hl7.davinci.r4.crdhook.orderselect.OrderSelectRequest;
import org.hl7.fhir.r4.model.Coding;
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
public class OrderSelectServiceTest {

  static final Logger logger = LoggerFactory.getLogger(OrderSelectServiceTest.class);
  @Autowired
  private OrderSelectService service;

  private Coding getMethotrexateCode() {
    return new Coding().setCode("105585")
        .setSystem("http://www.nlm.nih.gov/research/umls/rxnorm")
        .setDisplay("methotrexate 2.5 MG Oral Tablet");
  }

  private Coding getAzathioprineCode() {
    return new Coding().setCode("105611")
        .setSystem("http://www.nlm.nih.gov/research/umls/rxnorm")
        .setDisplay("azathioprine 50 MG Oral Tablet [Imuran]");
  }

  private Coding getTylenolCode() {
    return new Coding().setCode("209459")
        .setSystem("http://www.nlm.nih.gov/research/umls/rxnorm")
        .setDisplay("acetaminophen 500 MG Oral Tablet [Tylenol]");
  }

  @Test
  public void testHandleRequestWithNoMatch() {
    URL applicationBase;
    try {
      applicationBase = new URL("http","localhost","/");
    } catch (MalformedURLException e){
      throw new RuntimeException(e);
    }
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);

    // make sure azathioprine does not come back with an interaction with tylenol
    OrderSelectRequest request = CrdRequestCreator
        .createOrderSelectRequest(Enumerations.AdministrativeGender.MALE, cal.getTime(),
            "MA", "MA", getAzathioprineCode(), getTylenolCode());

    CdsResponse response = service.handleRequest(request, applicationBase);
    assertNotNull(response);
    assertEquals(0, response.getCards().size());
    assertTrue(response.getCards().isEmpty());
  }

  @Test
  public void testHandleRequestWithMatch() {
    URL applicationBase;
    try {
      applicationBase = new URL("http","localhost","/");
    } catch (MalformedURLException e){
      throw new RuntimeException(e);
    }
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);

    // make sure azathioprine comes back with an interaction with methotrexate
    OrderSelectRequest request = CrdRequestCreator
        .createOrderSelectRequest(Enumerations.AdministrativeGender.MALE, cal.getTime(),
            "MA", "MA", getAzathioprineCode(), getMethotrexateCode());

    CdsResponse response = service.handleRequest(request, applicationBase);
    assertNotNull(response);
    assertEquals(1, response.getCards().size());
    assertTrue(response.getCards().get(0).getSummary().contains("Drug Interaction Found"));
    assertEquals(Card.IndicatorEnum.WARNING, response.getCards().get(0).getIndicator());

    // make sure methotrexate comes back with an interaction with azathioprine
    request = CrdRequestCreator
        .createOrderSelectRequest(Enumerations.AdministrativeGender.MALE, cal.getTime(),
            "MA", "MA", getMethotrexateCode(), getAzathioprineCode());
    response = service.handleRequest(request, applicationBase);
    assertNotNull(response);
    assertEquals(1, response.getCards().size());
    assertTrue(response.getCards().get(0).getSummary().contains("Drug Interaction Found"));
    assertEquals(Card.IndicatorEnum.WARNING, response.getCards().get(0).getIndicator());
  }
}
