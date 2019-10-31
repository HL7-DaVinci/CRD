package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.cdshooks.Hook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundle;
import org.hl7.davinci.endpoint.cql.r4.CqlExecutionContextBuilder;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleFinder;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleQuery;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.opencds.cqf.cql.execution.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

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

  @Override
  public List<CoverageRequirementRuleResult> createCqlExecutionContexts(MedicationPrescribeRequest request, CoverageRequirementRuleFinder ruleFinder) {
    List<MedicationRequest> medicationRequestList = extractMedicationRequests(request);
    if (medicationRequestList.isEmpty()) {
      throw RequestIncompleteException.NoSupportedBundlesFound();
    }

    List<CoverageRequirementRuleResult> results = new ArrayList<>();
    results.addAll(getMedicationRequestExecutionContexts(medicationRequestList, ruleFinder));

    return results;
  }

  private Context createCqlExecutionContext(CqlBundle cqlPackage, MedicationRequest medicationRequest) {
    Patient patient = (Patient) medicationRequest.getSubject().getResource();
    HashMap<String,Resource> cqlParams = new HashMap<>();
    cqlParams.put("Patient", patient);
    cqlParams.put("medication_request", medicationRequest);
    return CqlExecutionContextBuilder.getExecutionContext(cqlPackage, cqlParams);
  }

  private List<CoverageRequirementRuleResult> getMedicationRequestExecutionContexts(List<MedicationRequest> medicationRequests, CoverageRequirementRuleFinder ruleFinder) {
    List<CoverageRequirementRuleResult> results = new ArrayList<>();
    for (MedicationRequest medicationRequest : medicationRequests) {
      List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(medicationRequest);
      for (CoverageRequirementRuleCriteria criteria : criteriaList) {
        CoverageRequirementRuleQuery query = new CoverageRequirementRuleQuery(ruleFinder, criteria);
        query.execute();
        for (CoverageRequirementRule rule: query.getResponse()) {
          CoverageRequirementRuleResult result = new CoverageRequirementRuleResult();
          result.setCriteria(criteria);
          try {
            result.setContext(createCqlExecutionContext(rule.getCqlBundle(), medicationRequest));
            results.add(result);
          } catch (Exception e) {
            logger.info("r4/MedicationPrescribeService::getDeviceRequestExecutionContexts: failed processing cql bundle: " + e.getMessage());
          }
        }
      }
    }
    return results;
  }

  private List<MedicationRequest> extractMedicationRequests(MedicationPrescribeRequest request) {
    Bundle medicationRequestBundle = request.getPrefetch().getMedicationRequestBundle();
    List<MedicationRequest> medicationRequestList = Utilities
        .getResourcesOfTypeFromBundle(MedicationRequest.class, medicationRequestBundle);
    return medicationRequestList;
  }

  private List<CoverageRequirementRuleCriteria> createCriteriaList(MedicationRequest medicationRequest) {
    try {
      List<Coding> codings = medicationRequest.getMedicationCodeableConcept().getCoding();
      List<Coverage> coverages = medicationRequest.getInsurance().stream()
          .map(reference -> (Coverage) reference.getResource()).collect(Collectors.toList());
      List<Organization> payors = Utilities.getPayors(coverages);
      List<CoverageRequirementRuleCriteria> criteriaList = CoverageRequirementRuleCriteria
          .createQueriesFromR4(codings, payors);
      return criteriaList;
    } catch (Exception e) {
      throw new RequestIncompleteException("Unable to parse list of codes, codesystems, and payors from a medication request.");
    }
  }
}
