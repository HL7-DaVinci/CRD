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
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Component("r4_OrderReviewService")
public class OrderReviewService extends
    CdsService<Bundle, DomainResource, Patient, CodeableConcept> {

  public static final String ID = "order-review-crd";
  public static final String TITLE = "order-review Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_REVIEW;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final Prefetch PREFETCH;
  static final Logger logger = LoggerFactory.getLogger(OrderReviewService.class);
  public static final String FHIRVERSION = "r4";
  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
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


  public List<Bundle> getOrderReviewBundles(OrderReviewRequest request) {
    ArrayList<Bundle> retList = new ArrayList<>();
    retList.add(request.getPrefetch().getDeviceRequestBundle());
    retList.add(request.getPrefetch().getServiceRequestBundle());
    return retList;
  }

  public CodeableConcept getCc(DomainResource deviceRequest) throws FHIRException {
    if (deviceRequest instanceof DeviceRequest) {
      return ((DeviceRequest) deviceRequest).getCodeCodeableConcept();
    } else if (deviceRequest instanceof ServiceRequest) {
      return ((ServiceRequest) deviceRequest).getCode();
    }
    DeviceRequest dev = (DeviceRequest) deviceRequest;
    return dev.getCodeCodeableConcept();
  }

  public Patient getPatient(DomainResource deviceRequest) {
    if (deviceRequest instanceof DeviceRequest) {
      return (Patient) ((DeviceRequest) deviceRequest).getSubject().getResource();
    } else if (deviceRequest instanceof ServiceRequest) {
      return (Patient) ((ServiceRequest) deviceRequest).getSubject().getResource();
    }
    return null;
  }

  @Override
  public List<DomainResource> getRequests(CdsRequest request) {
    OrderReviewRequest orderReviewRequest = (OrderReviewRequest) request;
    List<Bundle> allBundles = getOrderReviewBundles(orderReviewRequest);
    ArrayList<DomainResource> retList = new ArrayList<>();
    ArrayList<Class<? extends DomainResource>> types = new ArrayList<>();
    types.add(DeviceRequest.class);
    types.add(ServiceRequest.class);
    //Bundle orderReviewBundle = orderReviewRequest.getPrefetch().getDeviceRequestBundle();
    for (Bundle orderReviewBundle : allBundles) {
      if (orderReviewBundle != null) {
        Utilities util = new Utilities();
        retList.addAll(util.getResourcesOfTypesFromBundle(types, orderReviewBundle));
      }
    }
    if (retList.size() > 0) {
      return retList;
    } else {
      return null;
    }
  }

}
