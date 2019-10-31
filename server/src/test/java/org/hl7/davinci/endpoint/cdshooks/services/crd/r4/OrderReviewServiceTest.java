package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import org.cdshooks.CdsResponse;
import org.hl7.davinci.FatalRequestIncompleteException;
import org.hl7.davinci.r4.CrdRequestCreator;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.orderreview.OrderReviewRequest;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderReviewServiceTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  @Autowired
  private OrderReviewService service;

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
    OrderReviewRequest request = CrdRequestCreator
        .createOrderReviewRequest(Enumerations.AdministrativeGender.MALE, cal.getTime(), "MA",
            "MA");
    CdsResponse response = service.handleRequest(request, applicationBase);
    assertNotNull(response);
    assertEquals(1, response.getCards().size());
    assertEquals("Auth required",
        response.getCards().get(0).getSummary());
  }

  @Test
  public void testPrefetchNeededNoFhirServerThrowsError() {
    URL applicationBase;
    try {
      applicationBase = new URL("http","localhost","/");
    } catch (MalformedURLException e){
      throw new RuntimeException(e);
    }
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    OrderReviewRequest request = CrdRequestCreator
        .createOrderReviewRequest(Enumerations.AdministrativeGender.MALE, cal.getTime(), "MA",
            "MA");
    request.setPrefetch(new CrdPrefetch()); //empty the prefetch
    request.setFhirServer(null); //empty the fhir server

    thrown.expect(FatalRequestIncompleteException.class);
    service.handleRequest(request, applicationBase);
  }

  @Test
  public void testPrefetchNeededNoResourceIdThrowsError() {
    URL applicationBase;
    try {
      applicationBase = new URL("http","localhost","/");
    } catch (MalformedURLException e){
      throw new RuntimeException(e);
    }
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    OrderReviewRequest request = CrdRequestCreator
        .createOrderReviewRequest(Enumerations.AdministrativeGender.MALE, cal.getTime(), "MA",
            "MA");
    request.setPrefetch(new CrdPrefetch()); //empty the prefetch
    request.getContext().getOrders().getEntryFirstRep().getResource().setId((IIdType) null);

    thrown.expect(FatalRequestIncompleteException.class);
    service.handleRequest(request, applicationBase);
  }
}
