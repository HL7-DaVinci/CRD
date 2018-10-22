package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cdshooks.Hook;
import org.hl7.davinci.PatientInfo;
import org.hl7.davinci.PractitionerRoleInfo;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleQuery;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.orderreview.OrderReviewRequest;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component("r4_OrderReviewService")
public class OrderReviewService extends CdsService<OrderReviewRequest> {

  public static final String ID = "order-review-crd";
  public static final String TITLE = "order-review Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_REVIEW;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
      CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
      CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.SERVICE_REQUEST_BUNDLE);
  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  static final Logger logger = LoggerFactory.getLogger(OrderReviewService.class);

  public OrderReviewService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS);
  }

  public List<Bundle> getOrderReviewBundles(OrderReviewRequest request) {
    ArrayList<Bundle> retList = new ArrayList<>();
    retList.add(request.getPrefetch().getDeviceRequestBundle());
    retList.add(request.getPrefetch().getServiceRequestBundle());
    return retList;
  }

  public List<CoverageRequirementRuleQuery> makeQueries(OrderReviewRequest orderReviewRequest)
      throws RequestIncompleteException {
    List<CoverageRequirementRuleQuery> queries = new ArrayList<>();
    List<Bundle> allBundles = getOrderReviewBundles(orderReviewRequest);
    ArrayList<DomainResource> retList = new ArrayList<>();
    ArrayList<Class<? extends DomainResource>> types = new ArrayList<>();
    types.add(DeviceRequest.class);
    types.add(ServiceRequest.class);
    for (Bundle orderReviewBundle : allBundles) {
      if (orderReviewBundle != null) {
        Utilities util = new Utilities();
        retList.addAll(util.getResourcesOfTypesFromBundle(types, orderReviewBundle));
      }
    }

    for (DomainResource genericRequest : retList) {
      List<Coding> codings = null;
      Patient patient = null;
      PractitionerRole practitionerRole = null;
      PatientInfo patientInfo = null;
      PractitionerRoleInfo practitionerRoleInfo = null;
      try {
        if (genericRequest instanceof DeviceRequest) {
          DeviceRequest deviceRequest = (DeviceRequest) genericRequest;
          codings = deviceRequest.getCodeCodeableConcept().getCoding();
          patient = (Patient) deviceRequest.getSubject().getResource();

          practitionerRole = (PractitionerRole) deviceRequest.getPerformer().getResource();
        } else if (genericRequest instanceof ServiceRequest) {
          ServiceRequest deviceRequest = (ServiceRequest) genericRequest;
          codings = deviceRequest.getCode().getCoding();
          patient = (Patient) deviceRequest.getSubject().getResource();
          practitionerRole = (PractitionerRole) deviceRequest.getPerformer().get(0).getResource();
        }
        patientInfo = Utilities.getPatientInfo(patient);
        practitionerRoleInfo = Utilities.getPractitionerRoleInfo(practitionerRole);

        queries.addAll(
            this.resourcesToQueries(codings, patient == null, practitionerRole == null, patientInfo,
                practitionerRoleInfo));
      } catch (RequestIncompleteException e) {
        throw e;
      } catch (Exception e) {
        logger.error("Error parsing needed info from the device request bundle.", e);
      }
    }
    return queries;
  }

}
