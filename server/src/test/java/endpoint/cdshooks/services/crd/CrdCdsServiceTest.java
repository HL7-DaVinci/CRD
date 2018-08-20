package endpoint.cdshooks.services.crd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Calendar;

import org.hl7.davinci.CrdRequestCreator;
import org.hl7.davinci.cdshooks.CdsResponse;
import org.hl7.davinci.cdshooks.orderreview.CrdCdsRequest;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.Test;


public class CrdCdsServiceTest {
  @Test
  public void testHandleRequest() {
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    CrdCdsRequest request = CrdRequestCreator.createRequest(Enumerations.AdministrativeGender.MALE, cal.getTime());
    CrdCdsService service = new CrdCdsService();
    CdsResponse response = service.handleRequest(request);
    assertNotNull(response);
    assertEquals(1, response.getCards().size());
    assertEquals("empty card", response.getCards().get(0).getSummary());
  }
}
