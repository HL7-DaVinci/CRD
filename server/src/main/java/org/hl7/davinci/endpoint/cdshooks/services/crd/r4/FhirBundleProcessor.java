package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cql.CqlRule;
import org.hl7.davinci.endpoint.cql.r4.CqlExecutionContextBuilder;
import org.hl7.davinci.endpoint.database.RuleMapping;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.fhir.r4.model.*;
import org.opencds.cqf.cql.execution.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FhirBundleProcessor {
  static final Logger logger = LoggerFactory.getLogger(FhirBundleProcessor.class);

  private FileStore fileStore;
  private CrdPrefetch prefetch;
  private String baseUrl;
  private List<String> selections;
  private List<CoverageRequirementRuleResult> results = new ArrayList<>();


  public FhirBundleProcessor(CrdPrefetch prefetch, FileStore fileStore, String baseUrl, List<String> selections) {
    this.prefetch = prefetch;
    this.fileStore = fileStore;
    this.baseUrl = baseUrl;
    this.selections = selections;
  }

  public FhirBundleProcessor(CrdPrefetch prefetch, FileStore fileStore, String baseUrl) {
    this(prefetch, fileStore, baseUrl, new ArrayList<>());
  }

  public List<CoverageRequirementRuleResult> getResults() { return results; }

  public void processDeviceRequests() {
    Bundle deviceRequestBundle = prefetch.getDeviceRequestBundle();
    List<DeviceRequest> deviceRequestList = Utilities.getResourcesOfTypeFromBundle(DeviceRequest.class, deviceRequestBundle);
    if (!deviceRequestList.isEmpty()) {
      logger.info("r4/FhirBundleProcessor::getAndProcessDeviceRequests: DeviceRequest(s) found");

      for (DeviceRequest deviceRequest : deviceRequestList) {
        if (idInSelectionsList(deviceRequest.getId())) {
          List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(deviceRequest.getCodeCodeableConcept(), deviceRequest.getInsurance());
          buildExecutionContexts(criteriaList, (Patient) deviceRequest.getSubject().getResource(), "device_request", deviceRequest);
        }
      }
    }
  }

  public void processMedicationRequests() {
    Bundle medicationRequestBundle = prefetch.getMedicationRequestBundle();
    List<MedicationRequest> medicationRequestList = Utilities.getResourcesOfTypeFromBundle(MedicationRequest.class, medicationRequestBundle);
    if (!medicationRequestList.isEmpty()) {
      logger.info("r4/FhirBundleProcessor::getAndProcessMedicationRequests: MedicationRequest(s) found");

      for (MedicationRequest medicationRequest : medicationRequestList) {
        if (idInSelectionsList(medicationRequest.getId())) {
          List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(medicationRequest.getMedicationCodeableConcept(), medicationRequest.getInsurance());
          buildExecutionContexts(criteriaList, (Patient) medicationRequest.getSubject().getResource(), "medication_request", medicationRequest);
        }
      }
    }
  }

  public void processMedicationDispenses() {
    Bundle medicationDispenseBundle = prefetch.getMedicationDispenseBundle();
    List<MedicationDispense> medicationDispenseList = Utilities.getResourcesOfTypeFromBundle(MedicationDispense.class, medicationDispenseBundle);
    if (!medicationDispenseList.isEmpty()) {
      logger.info("r4/FhirBundleProcessor::getAndProcessMedicationDispenses: MedicationDispense(s) found");

      for (MedicationDispense medicationDispense : medicationDispenseList) {
        if (idInSelectionsList(medicationDispense.getId())) {
          List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(medicationDispense.getMedicationCodeableConcept(), null);
          buildExecutionContexts(criteriaList, (Patient) medicationDispense.getSubject().getResource(), "medication_dispense", medicationDispense);
        }
      }
    }
  }

  public void processServiceRequests() {
    Bundle serviceRequestBundle = prefetch.getServiceRequestBundle();
    List<ServiceRequest> serviceRequestList = Utilities.getResourcesOfTypeFromBundle(ServiceRequest.class, serviceRequestBundle);
    if (!serviceRequestList.isEmpty()) {
      logger.info("r4/FhirBundleProcessor::getAndProcessServiceRequests: ServiceRequest(s) found");

      for (ServiceRequest serviceRequest : serviceRequestList) {
        if (idInSelectionsList(serviceRequest.getId())) {
          List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(serviceRequest.getCode(), serviceRequest.getInsurance());
          buildExecutionContexts(criteriaList, (Patient) serviceRequest.getSubject().getResource(), "service_request", serviceRequest);
        }
      }
    }
  }

  private List<CoverageRequirementRuleCriteria> createCriteriaList(CodeableConcept codeableConcept, List<Reference> insurance) {
    try {
      List<Coding> codings = codeableConcept.getCoding();
      if (codings.size() > 0) {
        logger.info("r4/FhirBundleProcessor::createCriteriaList: code[0]: " + codings.get(0).getCode() + " - " + codings.get(0).getSystem());
      } else {
        logger.info("r4/FhirBundleProcessor::createCriteriaList: empty codes list!");
      }

      List<Organization> payors = new ArrayList<>();
      if (insurance != null) {
        List<Coverage> coverages = insurance.stream()
            .map(reference -> (Coverage) reference.getResource()).collect(Collectors.toList());
        payors = Utilities.getPayors(coverages);
      }
      if (payors.size() > 0) {
        logger.info("r4/FhirBundleProcessor::createCriteriaList: payer[0]: " + payors.get(0).getName());
      } else {
        // default to CMS if no payer was provided
        logger.info("r4/FhirBundleProcessor::createCriteriaList: empty payers list, working around by adding CMS!");
        Organization org = new Organization().setName("Centers for Medicare and Medicaid Services");
        org.setId("75f39025-65db-43c8-9127-693cdf75e712"); // how to get ID
        payors.add(org);
        // remove the exception to use CMS if no payer is provided
        // JIRA ticket https://jira.mitre.org/browse/DMEERX-894
        // throw new RequestIncompleteException("No Payer found in coverage resource, cannot find documentation.");
      }

      List<CoverageRequirementRuleCriteria> criteriaList = CoverageRequirementRuleCriteria
          .createQueriesFromR4(codings, payors);
      return criteriaList;
    } catch (RequestIncompleteException e) {
      // rethrow incomplete request exceptions
      throw e;
    } catch (Exception e) {
      // catch all remaining exceptions
      System.out.println(e);
      throw new RequestIncompleteException("Unable to parse list of codes, codesystems, and payors from a device request.");
    }
  }

  private void buildExecutionContexts(List<CoverageRequirementRuleCriteria> criteriaList, Patient patient, String requestType, DomainResource request) {
    for (CoverageRequirementRuleCriteria criteria : criteriaList) {
      logger.info("FhirBundleProcessor::buildExecutionContexts() criteria: " + criteria.toString());
      List<RuleMapping> rules = fileStore.findRules(criteria);

      for (RuleMapping rule: rules) {
        CoverageRequirementRuleResult result = new CoverageRequirementRuleResult();
        result.setCriteria(criteria).setTopic(rule.getTopic());
        try {
          logger.info("FhirBundleProcessor::buildExecutionContexts() found rule topic: " + rule.getTopic());

          //get the CqlRule
          CqlRule cqlRule = fileStore.getCqlRule(rule.getTopic(), rule.getFhirVersion());
          result.setContext(createCqlExecutionContext(cqlRule, patient, requestType, request));
          results.add(result);
        } catch (Exception e) {
          logger.info("r4/FhirBundleProcessor::buildExecutionContexts: failed processing cql bundle: " + e.getMessage());
        }
      }
    }
  }

  private Context createCqlExecutionContext(CqlRule cqlRule, Patient patient, String requestType, DomainResource request) {
    HashMap<String, Resource> cqlParams = new HashMap<>();
    cqlParams.put("Patient", patient);
    cqlParams.put(requestType, request);
    return CqlExecutionContextBuilder.getExecutionContext(cqlRule, cqlParams, baseUrl);
  }

  private String stripResourceType(String identifier) {
    int indexOfDivider = identifier.indexOf('/');
    if (indexOfDivider+1 == identifier.length()) {
      // remove the trailing '/'
      return identifier.substring(0, indexOfDivider);
    } else {
      return identifier.substring(indexOfDivider+1);
    }
  }

  private boolean idInSelectionsList(String identifier) {
    if (this.selections.isEmpty()) {
      // if selections list is empty, just assume we should process the request
      return true;
    } else {
      for ( String selection : selections) {
        if (identifier.contains(stripResourceType(selection))) {
          logger.info("r4/FhirBundleProcessor::idInSelectionsList(" + identifier + "): identifier found in selections list");
          return true;
        }
      }
      return false;
    }
  }

}
