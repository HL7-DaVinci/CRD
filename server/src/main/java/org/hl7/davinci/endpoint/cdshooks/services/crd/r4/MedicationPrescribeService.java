package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.Arrays;
import java.util.List;
import org.cdshooks.Hook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleFinder;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.opencds.cqf.cql.execution.Context;
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
//  public List<CoverageRequirementRuleQuery> makeQueries(
//      MedicationPrescribeRequest orderReviewRequest)
//      throws RequestIncompleteException {
//    List<CoverageRequirementRuleQuery> queries = new ArrayList<>();
//    Bundle medicationRequestBundle = orderReviewRequest.getPrefetch().getMedicationRequestBundle();
//    List<MedicationRequest> medicationRequestList = Utilities
//        .getResourcesOfTypeFromBundle(MedicationRequest.class, medicationRequestBundle);
//    for (MedicationRequest medicationRequest : medicationRequestList) {
//      List<Coding> codings = null;
//      Patient patient = null;
//      PractitionerRole practitionerRole = null;
//      PatientInfo patientInfo = null;
//      PractitionerRoleInfo practitionerRoleInfo = null;
//      try {
//        codings = medicationRequest.getMedicationCodeableConcept().getCoding();
//        patient = (Patient) medicationRequest.getSubject().getResource();
//        practitionerRole = (PractitionerRole) medicationRequest.getPerformer().getResource();
//
//        patientInfo = Utilities.getPatientInfo(patient);
//        practitionerRoleInfo = Utilities.getPractitionerRoleInfo(practitionerRole);
//
//        queries.addAll(
//            this.resourcesToQueries(codings, patient == null, practitionerRole == null, patientInfo,
//                practitionerRoleInfo));
//      } catch (RequestIncompleteException e) {
//        throw e;
//      } catch (Exception e) {
//        logger.error("Error parsing needed info from the device request bundle.", e);
//      }
//    }
//    return queries;
//  }

  public List<CoverageRequirementRuleResult> createCqlExecutionContexts(MedicationPrescribeRequest request, CoverageRequirementRuleFinder ruleFinder)
      throws RequestIncompleteException {
    throw new RuntimeException("Not implemented yet");
  }
}
