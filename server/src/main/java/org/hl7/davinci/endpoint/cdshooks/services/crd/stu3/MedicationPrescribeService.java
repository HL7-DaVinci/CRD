package org.hl7.davinci.endpoint.cdshooks.services.crd.stu3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.cdshooks.Hook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.components.AbstractCrdRuleQuery;
import org.hl7.davinci.endpoint.components.AbstractCrdRuleQueryFactory;
import org.hl7.davinci.endpoint.cql.CqlExecutionContextBuilder;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleCriteria;
import org.hl7.davinci.stu3.FhirComponents;
import org.hl7.davinci.stu3.Utilities;
import org.hl7.davinci.stu3.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.stu3.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.davinci.stu3.fhirresources.DaVinciMedicationRequest;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Coverage;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Resource;
import org.opencds.cqf.cql.execution.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

  public MedicationPrescribeService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS);
  }

  @Override
  public List<Context> createCqlExecutionContexts(MedicationPrescribeRequest request, AbstractCrdRuleQueryFactory ruleQueryFactory) {

    List<DaVinciMedicationRequest> medicationRequestList = extractMedicationRequests(request);
    if (medicationRequestList.isEmpty()) {
      throw RequestIncompleteException.NoSupportedBundlesFound();
    }

    List<Context> contexts = new ArrayList<>();
    contexts.addAll(getMedicationRequestExecutionContexts(medicationRequestList, ruleQueryFactory));

    return contexts;
  }

  private Context createCqlExecutionContext(String cql, DaVinciMedicationRequest medicationRequest) {
    Patient patient = (Patient) medicationRequest.getSubject().getResource();
    PractitionerRole practitionerRole = (PractitionerRole) medicationRequest.getRequester().getAgent().getResource();
    Location practitionerLocation = (Location) practitionerRole.getLocation().get(0).getResource();
    HashMap<String,Resource> cqlParams = new HashMap<>();
    cqlParams.put("Patient", patient);
    cqlParams.put("medication_request", medicationRequest);
    cqlParams.put("practitioner_location", practitionerLocation);
    return CqlExecutionContextBuilder.getExecutionContextStu3(cql, cqlParams);
  }

  private List<Context> getMedicationRequestExecutionContexts(List<DaVinciMedicationRequest> medicationRequests, AbstractCrdRuleQueryFactory ruleQueryFactory) {
    List<Context> contexts = new ArrayList<>();
    for (DaVinciMedicationRequest medicationRequest : medicationRequests) {
      List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(medicationRequest);
      for (CoverageRequirementRuleCriteria criteria : criteriaList) {
        AbstractCrdRuleQuery query = ruleQueryFactory.create(criteria);
        List<String> cqlList = query.getCql();
        for (String cql: cqlList) {
          contexts.add(createCqlExecutionContext(cql, medicationRequest));
        }
      }
    }
    return contexts;
  }

  private List<DaVinciMedicationRequest> extractMedicationRequests(MedicationPrescribeRequest request) {
    Bundle medicationRequestBundle = request.getPrefetch().getMedicationRequestBundle();
    List<DaVinciMedicationRequest> medicationRequestList = Utilities
        .getResourcesOfTypeFromBundle(DaVinciMedicationRequest.class, medicationRequestBundle);
    return medicationRequestList;
  }

  private List<CoverageRequirementRuleCriteria> createCriteriaList(DaVinciMedicationRequest medicationRequest) {
    try {
      List<Coding> codings = medicationRequest.getMedicationCodeableConcept().getCoding();
      List<Coverage> coverages = medicationRequest.getInsurance().stream()
          .map(reference -> (Coverage) reference.getResource()).collect(Collectors.toList());
      List<Organization> payors = Utilities.getPayors(coverages);
      List<CoverageRequirementRuleCriteria> criteriaList = CoverageRequirementRuleCriteria
          .createQueriesFromStu3(codings, payors);
      return criteriaList;
    } catch (Exception e) {
      throw new RequestIncompleteException("Unable to parse list of codes, codesystems, and payors from a medication request.");
    }
  }
}
