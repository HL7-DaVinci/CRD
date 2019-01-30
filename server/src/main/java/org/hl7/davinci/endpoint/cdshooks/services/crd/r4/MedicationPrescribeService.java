package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cdshooks.Hook;
import org.hl7.davinci.PatientInfo;
import org.hl7.davinci.PractitionerRoleInfo;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleQuery;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("r4_MedicationPrescribeService")
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

  public MedicationPrescribeService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS);
  }

  /**
   * Acquires the specific information needed by the parent request handling
   * function.
   * @param orderReviewRequest the request to extract information from
   * @return a list of the information required.
   * @throws RequestIncompleteException if the request cannot be parsed.
   */
  public List<CoverageRequirementRuleQuery> makeQueries(
      MedicationPrescribeRequest orderReviewRequest, YamlConfig options)
      throws RequestIncompleteException {
    List<CoverageRequirementRuleQuery> queries = new ArrayList<>();
    Bundle medicationRequestBundle = orderReviewRequest.getPrefetch().getMedicationRequestBundle();
    List<MedicationRequest> medicationRequestList = Utilities
        .getResourcesOfTypeFromBundle(MedicationRequest.class, medicationRequestBundle);
    for (MedicationRequest medicationRequest : medicationRequestList) {
      List<Coding> codings = null;
      Patient patient = null;
      PractitionerRole practitionerRole = null;
      PatientInfo patientInfo = null;
      PractitionerRoleInfo practitionerRoleInfo = null;
      try {
        if (medicationRequest.hasMedicationCodeableConcept()) {
          codings = medicationRequest.getMedicationCodeableConcept().getCoding();
        } else {
          throw new RequestIncompleteException("Request bundle is missing medication code.");
        }
        patient = (Patient) medicationRequest.getSubject().getResource();
        practitionerRole = (PractitionerRole) medicationRequest.getPerformer().getResource();
        if (practitionerRole == null) {
          throw new RequestIncompleteException("Cannot find practitioner role " + medicationRequest
              .getPerformer()
              .getReference());
        }
        patientInfo = Utilities.getPatientInfo(patient);
        practitionerRoleInfo = Utilities.getPractitionerRoleInfo(practitionerRole, options.isCheckPractitionerLocation());

        queries.addAll(
            this.resourcesToQueries(codings, patient == null, practitionerRole == null, patientInfo,
                practitionerRoleInfo));
      } catch (RequestIncompleteException e) {
        throw e;
      } catch (FHIRException e) {
        logger.error("Failed to parse medication request bundle", e);
      }
    }
    return queries;
  }
}
