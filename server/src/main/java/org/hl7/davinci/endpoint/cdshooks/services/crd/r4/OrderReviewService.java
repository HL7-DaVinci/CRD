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
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleFinder;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.orderreview.OrderReviewRequest;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  private CoverageRequirementRuleFinder ruleFinder;

  public OrderReviewService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS);
  }

  public List<CoverageRequirementRule> findRules(OrderReviewRequest orderReviewRequest)
      throws RequestIncompleteException {
    Boolean parsedAtLeastOneResource = false;
    List<CoverageRequirementRule> coverageRequirementRules = new ArrayList<>();

    Bundle deviceRequestBundle = orderReviewRequest.getPrefetch().getDeviceRequestBundle();
    List<DeviceRequest> deviceRequestList = Utilities
        .getResourcesOfTypeFromBundle(DeviceRequest.class, deviceRequestBundle);
    for (DeviceRequest deviceRequest : deviceRequestList) {

      List<Coding> codings = null;
      Patient patient = null;
      PractitionerRole practitionerRole = null;
      PatientInfo patientInfo = null;
      PractitionerRoleInfo practitionerRoleInfo = null;
      try {
        codings = deviceRequest.getCodeCodeableConcept().getCoding();
        patient = (Patient) deviceRequest.getSubject().getResource();
        practitionerRole = (PractitionerRole) deviceRequest.getPerformer().getResource();

        patientInfo = Utilities.getPatientInfo(patient);
        practitionerRoleInfo = Utilities.getPractitionerRoleInfo(practitionerRole);
      } catch (RequestIncompleteException e) {
        throw e;
      } catch (Exception e) {
        logger.error("Error parsing needed info from the device request bundle.", e);
      }
      if (codings == null || codings.size() == 0) {
        throw new RequestIncompleteException("Unable to parse a device code out of the request.");
      }
      if (patient == null) {
        throw new RequestIncompleteException("No patient could be (pre)fetched in this request.");
      }
      if (practitionerRole == null) {
        throw new RequestIncompleteException("Unable to find the practitioner role.");
      }
      for (Coding coding : codings) {
        if (coding.getCode() == null || coding.getSystem() == null) {
          logger.error("Found coding with a null code or system.");
          continue;
        }
        parsedAtLeastOneResource = true;
        coverageRequirementRules.addAll(ruleFinder
            .findRules(patientInfo.getPatientAge(), patientInfo.getPatientGenderCode(),
                coding.getCode(),
                coding.getSystem(), patientInfo.getPatientAddressState(),
                practitionerRoleInfo.getLocationAddressState()));
      }
    }

    if (!parsedAtLeastOneResource) {
      throw new RequestIncompleteException("Unable to (pre)fetch any supported resources from the bundle.");
    }

    return coverageRequirementRules;
  }

}
