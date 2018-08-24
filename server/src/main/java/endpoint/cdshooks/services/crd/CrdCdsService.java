package endpoint.cdshooks.services.crd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import endpoint.components.CardBuilder;
import endpoint.components.FhirComponents;

import java.util.LinkedHashMap;
import java.util.List;

import javax.validation.Valid;

import endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.cdshooks.CdsResponse;
import org.hl7.davinci.cdshooks.CdsService;
import org.hl7.davinci.cdshooks.Hook;
import org.hl7.davinci.cdshooks.Prefetch;

import org.hl7.davinci.cdshooks.orderreview.CrdCdsRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;


@Component
public class CrdCdsService extends CdsService {

  static final Logger logger = LoggerFactory.getLogger(CrdCdsService.class);

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
    IGenericClient client = composeClient(request);
    logger.info("handleRequest: start");
    logger.info("Order bundle size: " + request.getContext().getOrders().getEntry().size());
    DeviceRequest deviceRequest = null;
    for (Bundle.BundleEntryComponent bec: request.getContext().getOrders().getEntry()) {
      if (bec.hasResource()) {
        deviceRequest = (DeviceRequest) bec.getResource();


        String pip = deviceRequest.getSubject().getReference();
        // Change from regex to something more robust in getting references.
        String[] resourceToGet = pip.split("/");
        IBaseResource fhirResource = client.read()
            .resource(resourceToGet[0])
            .withId(resourceToGet[1])
            .execute();
      }
    }

    if (request.getPrefetch().getPatient() != null) {
      logger.info("handleRequest: patient birthdate: "
          + request.getPrefetch().getPatient().getBirthDate().toString());
    }
    if (request.getPrefetch().getCoverage() != null) {
      logger.info("handleRequest: coverage id: "
          + request.getPrefetch().getCoverage().getId());
    }
    if (request.getPrefetch().getLocation() != null) {
      logger.info("handleRequest: location address: "
          + request.getPrefetch().getLocation().getAddress().getCity() + ", "
          + request.getPrefetch().getLocation().getAddress().getState());
    }
    if (request.getPrefetch().getInsurer() != null) {
      logger.info("handleRequest: insurer id: "
          + request.getPrefetch().getInsurer().getName());
    }
    if (request.getPrefetch().getProvider() != null) {
      logger.info("handleRequest: provider name: "
          + request.getPrefetch().getProvider().getName().get(0).getPrefixAsSingleString() + " "
          + request.getPrefetch().getProvider().getName().get(0).getFamily());
    }

    if (deviceRequest == null) {
      // TODO: raise error
      logger.error("No request provided!");
    }

    String msg = "response";

    List<Annotation> list = deviceRequest.getNote();
    if (!list.isEmpty()) {
      msg = deviceRequest.getNote().get(0).getText();
      logger.info("handleRequest: " + deviceRequest.getNote().get(0).getText());
    } else {
      logger.info("handleRequest: no notes specified");
    }

    CdsResponse response = new CdsResponse();

    // TODO - Replace this with database lookup logic
    response.addCard(CardBuilder.summaryCard("Responses from this service are currently hard coded."));

    CoverageRequirementRule crr = new CoverageRequirementRule();
    crr.setAgeRangeHigh(80);
    crr.setAgeRangeLow(55);
    crr.setEquipmentCode("E0424");
    crr.setGenderCode("F".charAt(0));
    crr.setNoAuthNeeded(false);
    crr.setInfoLink("https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/"
        + "MLNProducts/Downloads/Home-Oxygen-Therapy-Text-Only.pdf");

    response.addCard(CardBuilder.transform(crr));
    logger.info("handleRequest: end");
    return response;
  }

  private IGenericClient composeClient(CrdCdsRequest request) {
    String serverBase = request.getFhirServer();
    FhirContext ctx = FhirContext.forR4();
    LinkedHashMap<String,String> oauth =  (LinkedHashMap) request.getOauth();
    BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(oauth.get("access_token"));

    IGenericClient client = ctx.newRestfulGenericClient(serverBase);
    client.registerInterceptor(authInterceptor);
    return client;
  }


}
