package org.hl7.davinci.endpoint.cdshooks.services.crd.stu3;

import java.util.ArrayList;
import java.util.Arrays;
import org.cdshooks.Hook;
import org.hl7.davinci.PatientInfo;
import org.hl7.davinci.PractitionerRoleInfo;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleFinder;
import org.hl7.davinci.stu3.FhirComponents;
import org.hl7.davinci.stu3.Utilities;
import org.hl7.davinci.stu3.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.stu3.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.davinci.stu3.fhirresources.DaVinciMedicationRequest;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("stu3_MedicationPrescribeService")
public class MedicationPrescribeService extends CdsService<MedicationPrescribeRequest> {

  public static final String ID = "medication-prescribe-crd";
  public static final String TITLE = "medication-prescribe Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.MEDICATION_PRESCRIBE;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays
      .asList(CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE);
  public static final Logger logger = LoggerFactory.getLogger(MedicationPrescribeService.class);

  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();

  @Autowired
  CoverageRequirementRuleFinder ruleFinder;

  public MedicationPrescribeService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS);
  }

  public List<CoverageRequirementRule> findRules(MedicationPrescribeRequest medicationPrescribeRequest)
      throws RequestIncompleteException {
    Boolean parsedAtLeastOneResource = false;
    List<CoverageRequirementRule> coverageRequirementRules = new ArrayList<>();

    Bundle medicationRequestBundle = medicationPrescribeRequest.getPrefetch().getMedicationRequestBundle();
    List<DaVinciMedicationRequest> medicationRequestList = Utilities
        .getResourcesOfTypeFromBundle(DaVinciMedicationRequest.class, medicationRequestBundle);
    for (DaVinciMedicationRequest medicationRequest : medicationRequestList) {

      List<Coding> codings = null;
      Patient patient = null;
      PatientInfo patientInfo = null;
      try {
        codings = medicationRequest.getMedicationCodeableConcept().getCoding();
        patient = (Patient) medicationRequest.getSubject().getResource();

        patientInfo = Utilities.getPatientInfo(patient);
      } catch (RequestIncompleteException e) {
        throw e;
      } catch (Exception e) {
        logger.error("Error parsing needed info from the medication request bundle.", e);
      }
      if (codings == null || codings.size() == 0) {
        throw new RequestIncompleteException("Unable to parse a medication code out of the request.");
      }
      if (patient == null) {
        throw new RequestIncompleteException("No patient could be (pre)fetched in this request.");
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
                null));
      }
    }

    if (!parsedAtLeastOneResource) {
      throw new RequestIncompleteException("Unable to (pre)fetch any supported resources from the bundle.");
    }

    return coverageRequirementRules;
  }
}
