package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

  @Ignore("No CQL R4 Support at this time.") @Test
  public void testHandleRequest() {
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    OrderReviewRequest request = CrdRequestCreator
        .createOrderReviewRequest(Enumerations.AdministrativeGender.MALE, cal.getTime(), "MA",
            "MA");
    CdsResponse response = service.handleRequest(request);
    assertNotNull(response);
    assertEquals(1, response.getCards().size());
    assertEquals("Documentation is required for the desired device or service",
        response.getCards().get(0).getSummary());
  }

  @Ignore("No CQL R4 Support at this time.") @Test
  public void testPrefetchNeededNoFhirServerThrowsError() {
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    OrderReviewRequest request = CrdRequestCreator
        .createOrderReviewRequest(Enumerations.AdministrativeGender.MALE, cal.getTime(), "MA",
            "MA");
    request.setPrefetch(new CrdPrefetch()); //empty the prefetch
    request.setFhirServer(null); //empty the fhir server

    thrown.expect(FatalRequestIncompleteException.class);
    service.handleRequest(request);
  }

  @Ignore("No CQL R4 Support at this time.") @Test
  public void testPrefetchNeededNoResourceIdThrowsError() {
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    OrderReviewRequest request = CrdRequestCreator
        .createOrderReviewRequest(Enumerations.AdministrativeGender.MALE, cal.getTime(), "MA",
            "MA");
    request.setPrefetch(new CrdPrefetch()); //empty the prefetch
    request.getContext().getOrders().getEntryFirstRep().getResource().setId((IIdType) null);

    thrown.expect(FatalRequestIncompleteException.class);
    service.handleRequest(request);
  }
}
