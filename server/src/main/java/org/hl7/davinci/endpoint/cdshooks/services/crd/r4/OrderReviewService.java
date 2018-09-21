package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import org.cdshooks.CdsRequest;
import org.cdshooks.Hook;
import org.cdshooks.Prefetch;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleFinder;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.CrdPrefetchTemplateElements.PrefetchTemplateElement;
import org.hl7.davinci.r4.crdhook.orderreview.OrderReviewRequest;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component("r4_OrderReviewService")
public class OrderReviewService extends
    CdsService<Bundle, DeviceRequest, Patient, CodeableConcept> {

  public static final String ID = "order-review-crd";
  public static final String TITLE = "order-review Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_REVIEW;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final Prefetch PREFETCH;
  static final Logger logger = LoggerFactory.getLogger(OrderReviewService.class);
  static final String FHIRVERSION = "r4";
  static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  static {
    PREFETCH = new Prefetch();
    List<PrefetchTemplateElement> elements = Arrays.asList(
        CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
        CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.SERVICE_REQUEST_BUNDLE
    );
    for (PrefetchTemplateElement element : elements) {
      PREFETCH.put(element.getKey(), element.getQuery());
    }
  }

  @Autowired
  CoverageRequirementRuleFinder ruleFinder;

  public OrderReviewService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH, FHIRCOMPONENTS, FHIRVERSION);
  }

  /**
   * Handle the post request to the service.
   *
   * @param request The json request, parsed.
   */
  public CdsResponse handleRequest(@Valid @RequestBody OrderReviewRequest request) {
    logger.info("handleRequest: start");
    logger.info("Order bundle size: " + request.getContext().getOrders().getEntry().size());

    //note currently we only use the device request if its in the prefetch or we get it into
    //the prefetch, so we dont use it if its just in the context since it wont have patient etc.
    FhirComponents fhirComponents = FhirComponents.getInstance();
    if (request.getPrefetch() == null)
      request.setPrefetch(new CrdPrefetch());
    PrefetchHydrator prefetchHydrator = new PrefetchHydrator<Bundle>(this, request,
        fhirComponents.getFhirContext());
    prefetchHydrator.hydrate(); //prefetch is now as hydrated as possible

    CdsResponse response = new CdsResponse();

    Bundle deviceRequestBundle = request.getPrefetch().getDeviceRequestBundle();
    if (deviceRequestBundle == null) {
      logger.error("Prefetch deviceRequestBundle not a bundle");
      response.addCard(CardBuilder.summaryCard(
          "deviceRequestBundle could not be (pre)fetched in this request "));
      return response;
    }
    List<DeviceRequest> deviceRequestList = Utilities.getResourcesOfTypeFromBundle(
        DeviceRequest.class, (Bundle) deviceRequestBundle);

  public CodeableConcept getCc(DeviceRequest deviceRequest) throws FHIRException {
    return deviceRequest.getCodeCodeableConcept();
  }

  public Patient getPatient(DeviceRequest deviceRequest) {
    return (Patient) deviceRequest.getSubject().getResource();
  }

  @Override
  public List<DeviceRequest> getRequests(CdsRequest request) {
    OrderReviewRequest orderReviewRequest = (OrderReviewRequest) request;
    Bundle orderReviewBundle = orderReviewRequest.getPrefetch().getDeviceRequestBundle();
    Utilities util = new Utilities();
    return util.getResourcesOfTypeFromBundle(DeviceRequest.class, orderReviewBundle);
  }
}
