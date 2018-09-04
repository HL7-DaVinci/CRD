package org.hl7.davinci.endpoint.cdshooks.services.crd;

import java.util.HashMap;
import java.util.List;
import org.hl7.davinci.Utilities;
import org.hl7.davinci.cdshooks.CrdPrefetch;
import org.hl7.davinci.cdshooks.PrefetchResponse;
import org.hl7.davinci.Utilities;
import org.hl7.davinci.cdshooks.orderreview.OrderReviewFetcher;
import org.hl7.davinci.endpoint.components.CardBuilder;

import javax.validation.Valid;

import org.hl7.davinci.endpoint.components.FhirComponents;
import org.hl7.davinci.endpoint.components.prefetchHydrator.PrefetchHydrator;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.cdshooks.CdsResponse;
import org.hl7.davinci.cdshooks.CdsService;
import org.hl7.davinci.cdshooks.Hook;
import org.hl7.davinci.cdshooks.Prefetch;

import org.hl7.davinci.cdshooks.orderreview.OrderReviewRequest;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleFinder;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;


@Component
public class OrderReviewService extends CdsService {

  static final Logger logger = LoggerFactory.getLogger(OrderReviewService.class);

  public static final String ID = "order-review-crd";
  public static final String TITLE = "order-review Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_REVIEW;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final Prefetch PREFETCH;
  static {
    PREFETCH = new Prefetch();
    PREFETCH.put(CrdPrefetch.supplyRequestBundleKey,CrdPrefetch.supplyRequestBundleQuery);
    PREFETCH.put(CrdPrefetch.serviceRequestBundleKey,CrdPrefetch.serviceRequestBundleQuery);
    PREFETCH.put(CrdPrefetch.nutritionOrderBundleKey,CrdPrefetch.nutritionOrderBundleQuery);
    PREFETCH.put(CrdPrefetch.medicationRequestBundleKey,CrdPrefetch.medicationRequestBundleQuery);
    PREFETCH.put(CrdPrefetch.deviceRequestBundleKey,CrdPrefetch.deviceRequestBundleQuery);
  }

  @Autowired
  CoverageRequirementRuleFinder ruleFinder;

  @Autowired
  FhirComponents fhirComponents;

  public OrderReviewService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH);
  }

  /**
   * Handle the post request to the service.
   * @param request The json request, parsed.
   * @return
   */
  public CdsResponse handleRequest(@Valid @RequestBody OrderReviewRequest request) {
    logger.info("handleRequest: start");
    logger.info("Order bundle size: " + request.getContext().getOrders().getEntry().size());

    //note currently we only use the device request if its in the prefetch or we get it into
    //the prefetch, so we dont use it if its just in the context since it wont have patient etc.

    PrefetchHydrator prefetchHydrator = new PrefetchHydrator(this, request, fhirComponents);
    prefetchHydrator.hydrate(); //prefetch is now as hydrated as possible

    CdsResponse response = new CdsResponse();

    IBaseResource drbResource = request.getPrefetch().getDeviceRequestBundle();
    if (drbResource == null  || drbResource.getClass() != Bundle.class) {
      logger.error("Prefetch DeviceRequestBundle not a bundle");
      response.addCard(CardBuilder.summaryCard(
          "DeviceRequestBundle could not be (pre)fetched in this request "));
      return response;
    }
    List<DeviceRequest> deviceRequestList = Utilities.getResourcesOfTypeFromBundle(
        DeviceRequest.class, (Bundle) drbResource);

    for (DeviceRequest deviceRequest: deviceRequestList) {

      Patient patient = null;
      CodeableConcept cc = null;
      try {
        cc = deviceRequest.getCodeCodeableConcept();
      } catch (FHIRException fe) {
        response.addCard(CardBuilder.summaryCard("Unable to parse the device code out of the request"));
      }
      try {
        patient = (Patient) deviceRequest.getSubject().getResource();
      } catch (Exception e) {
        response.addCard(CardBuilder.summaryCard("No patient could be (pre)fetched in this request"));
      }

      if (patient != null && cc != null) {
        int patientAge = Utilities.calculateAge(patient);
        CoverageRequirementRule crr = ruleFinder.findRule(patientAge, patient.getGender(), cc.getCoding().get(0).getCode());
        if (crr != null) {
          response.addCard(CardBuilder.transform(crr));
        } else {
          response.addCard(CardBuilder.summaryCard("No documentation rules found"));
        }
      }
    }

    logger.info("handleRequest: end");
    return response;
  }

}
