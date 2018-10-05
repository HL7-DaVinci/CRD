package org.hl7.davinci.endpoint.cdshooks.services.crd.stu3;

import org.cdshooks.CdsRequest;
import org.cdshooks.Hook;
import org.cdshooks.Prefetch;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleFinder;
import org.hl7.davinci.stu3.FhirComponents;
import org.hl7.davinci.stu3.Utilities;
import org.hl7.davinci.stu3.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.stu3.crdhook.CrdPrefetchTemplateElements.PrefetchTemplateElement;
import org.hl7.davinci.stu3.crdhook.orderreview.OrderReviewRequest;
import org.hl7.davinci.stu3.fhirresources.DaVinciDeviceRequest;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component("stu3_OrderReviewService")
public class OrderReviewService extends
    CdsService<Bundle, DaVinciDeviceRequest, Patient, CodeableConcept>  {

  public static final String ID = "order-review-crd";
  public static final String TITLE = "order-review Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_REVIEW;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final Prefetch PREFETCH;
  static final Logger logger = LoggerFactory.getLogger(OrderReviewService.class);
  public static final String FHIRVERSION = "stu3";
  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  static {
    PREFETCH = new Prefetch();
    List<PrefetchTemplateElement> elements = Arrays.asList(
        CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
        CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.PROCEDURE_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.REFERRAL_REQUEST_BUNDLE
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

  public CodeableConcept getCc(DaVinciDeviceRequest deviceRequest) throws FHIRException {
    return deviceRequest.getCodeCodeableConcept();
  }

  public Patient getPatient(DaVinciDeviceRequest deviceRequest) {
    return (Patient) deviceRequest.getSubject().getResource();
  }

  @Override
  public List<DaVinciDeviceRequest> getRequests(CdsRequest request) {
    OrderReviewRequest orderReviewRequest = (OrderReviewRequest) request;
    Bundle orderReviewBundle = orderReviewRequest.getPrefetch().getDeviceRequestBundle();
    if (orderReviewBundle == null) {
      return null;
    }
    Utilities util = new Utilities();
    return util.getResourcesOfTypeFromBundle(DaVinciDeviceRequest.class, orderReviewBundle);
  }

}
