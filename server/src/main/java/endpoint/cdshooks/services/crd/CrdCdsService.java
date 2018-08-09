package endpoint.cdshooks.services.crd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import endpoint.cdshooks.models.Card;
import endpoint.cdshooks.models.CdsResponse;
import endpoint.cdshooks.models.CdsService;
import endpoint.cdshooks.models.Hook;
import endpoint.cdshooks.models.Prefetch;
import endpoint.components.FhirComponents;
import javax.validation.Valid;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Component
public class CrdCdsService extends CdsService {

  public static final String ID = "coverage-requirements-discovery";
  public static final String TITLE = "Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_REVIEW;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final Prefetch PREFETCH = null;

  public CrdCdsService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH);
  }

  @Autowired
  private FhirComponents fhirComponents;

  /**
   * Handle the post request to the service.
   * @param request The json request, parsed.
   * @return
   */
  public CdsResponse handleRequest(@Valid @RequestBody CrdCdsRequest request) {
    DeviceRequest deviceRequest = null;
    for (String fhirResourceString : request.getContext().getOrdersFhirResourceStringList()) {
      IBaseResource baseResource = fhirComponents.getJsonParser().parseResource(fhirResourceString);
      if (baseResource.getClass() == DeviceRequest.class) {
        deviceRequest = (DeviceRequest) baseResource;
      }
    }

    if (deviceRequest == null) {
      // TODO: raise error
    }

    CdsResponse response = new CdsResponse();
    Card card = new Card();
    card.setSummary("empty card");
    response.addCard(card);
    return response;
  }
}
