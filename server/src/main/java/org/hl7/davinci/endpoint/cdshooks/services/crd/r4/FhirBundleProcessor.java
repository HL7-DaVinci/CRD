package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cql.CqlRule;
import org.hl7.davinci.endpoint.cql.r4.CqlExecutionContextBuilder;
import org.hl7.davinci.endpoint.database.RuleMapping;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.Utilities;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import java.util.Date;

public class FhirBundleProcessor {
  static final Logger logger = LoggerFactory.getLogger(FhirBundleProcessor.class);

  private FileStore fileStore;
  private String baseUrl;
  private List<String> selections;
  private List<CoverageRequirementRuleResult> results = new ArrayList<>();

  private boolean deidentifiedResourcesContainPhi = false;


  public FhirBundleProcessor(FileStore fileStore, String baseUrl, List<String> selections) {
    this.fileStore = fileStore;
    this.baseUrl = baseUrl;
    this.selections = selections;
  }

  public FhirBundleProcessor(FileStore fileStore, String baseUrl) {
    this(fileStore, baseUrl, new ArrayList<>());
  }

  public List<CoverageRequirementRuleResult> getResults() { return results; }

  public boolean getDeidentifiedResourceContainsPhi() { return deidentifiedResourcesContainPhi; }

  private boolean validateField(boolean empty, String field) {
    if (!empty) {
      logger.warn("Instance is claiming to be deidentified but found information in the " + field + " field.");
    }
    return empty;
  }

  public boolean verifyDeidentifiedPatient(Bundle bundle) {
    boolean invalid = false;
    List<Patient> patientList = Utilities.getResourcesOfTypeFromBundle(Patient.class, bundle);
    for (Patient patient: patientList) {
      Meta meta = patient.getMeta();
      for (CanonicalType profile : meta.getProfile()) {
        if (profile.equals("http://hl7.org/fhir/us/davinci-crd/StructureDefinition/profile-patient-deident")) {
          invalid |= validateField(patient.getText().isEmpty(), "patient.text");
          invalid |= validateField(patient.getIdentifier().isEmpty(), "patient.identifier");
          invalid |= validateField(patient.getName().isEmpty(), "patient.name");
          invalid |= validateField(patient.getTelecom().isEmpty(), "patient.telecom");
          invalid |= validateField(patient.getDeceased() == null, "patient.deceased");
          invalid |= validateField(patient.getMultipleBirth() == null, "patient.multipleBirth");
          invalid |= validateField(patient.getPhoto().isEmpty(), "patient.photo");
          invalid |= validateField(patient.getContact().isEmpty(), "patient.contact");
          invalid |= validateField(patient.getLink().isEmpty(), "patient.link");

          // check the address
          for (Address address : patient.getAddress()) {
            invalid |= validateField(address.getText() == null, "patient.address[].text");
            invalid |= validateField(address.getLine().isEmpty(), "patient.address[].line");
            invalid |= validateField(address.getCity() == null, "patient.address[].city");
            invalid |= validateField(address.getDistrict() == null, "patient.address[].district");
            invalid |= validateField(address.getPostalCode() == null, "patient.address[].postalCode");
            invalid |= validateField(address.getPeriod().isEmpty(), "patient.address[].period");
          }
          
          // check the birthdate
          Date now = new Date();
          long diffInMs = Math.abs(now.getTime() - patient.getBirthDate().getTime());
          long diffInDays = TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);
          String birthDateStr = patient.getBirthDateElement().asStringValue();

          // if age is less than 2 years then there should be a year and month
          if (diffInDays < (265 * 2)) {
            invalid |= validateField(birthDateStr.length() <= 7, "patient.birthDate day (" + birthDateStr + ")");
          } else {
            // otherwise there should only be a year
            invalid |= validateField(birthDateStr.length() <= 4, "patient.birthDate month (" + birthDateStr + ")");
          }
        }
      }
    }
    return invalid;
  }
  public boolean verifyDeidentifiedCoverage(Bundle bundle) {
    boolean invalid = false;
    List<Coverage> coverageList = Utilities.getResourcesOfTypeFromBundle(Coverage.class, bundle);
    for (Coverage coverage: coverageList) {
      Meta meta = coverage.getMeta();
      for (CanonicalType profile : meta.getProfile()) {
        if (profile.equals("http://hl7.org/fhir/us/davinci-crd/StructureDefinition/profile-coverage-deident")) {
          invalid |= validateField(coverage.getText().isEmpty(), "coverage.text");
          invalid |= validateField(coverage.getIdentifier().isEmpty(), "coverage.identifier");
          invalid |= validateField(coverage.getPolicyHolder().isEmpty(), "coverage.policyHolder");
          invalid |= validateField(coverage.getSubscriber().isEmpty(), "coverage.subscriber");
          invalid |= validateField(coverage.getSubscriberId() == null, "coverage.subscriberId");
          invalid |= validateField(coverage.getDependent() == null, "coverage.dependent");
          invalid |= validateField(coverage.getRelationship().isEmpty(), "coverage.relationship");
          invalid |= validateField(coverage.getOrder() <= 0, "coverage.order");
          invalid |= validateField(coverage.getNetwork() == null, "coverage.network");
          invalid |= validateField(coverage.getCostToBeneficiary().isEmpty(), "coverage.costToBeneficiary");
          invalid |= validateField(coverage.getContract().isEmpty(), "coverage.contract");
        }
      }
    }
    return invalid;
  }

  public boolean verifyDeidentifiedResources(Bundle bundle) {
    boolean invalid = verifyDeidentifiedPatient(bundle);
    invalid |= verifyDeidentifiedCoverage(bundle);
    deidentifiedResourcesContainPhi |= invalid;
    return invalid;
  }

  public void processDeviceRequests(Bundle deviceRequestBundle) {
    List<DeviceRequest> deviceRequestList = Utilities.getResourcesOfTypeFromBundle(DeviceRequest.class, deviceRequestBundle);
    if (!deviceRequestList.isEmpty()) {
      logger.info("r4/FhirBundleProcessor::processDeviceRequests: DeviceRequest(s) found");
      verifyDeidentifiedResources(deviceRequestBundle);

      for (DeviceRequest deviceRequest : deviceRequestList) {
        if (idInSelectionsList(deviceRequest.getId())) {
          List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(deviceRequest.getCodeCodeableConcept(), deviceRequest.getInsurance(), null);
          buildExecutionContexts(criteriaList, (Patient) deviceRequest.getSubject().getResource(), "device_request", deviceRequest);
        }
      }
    }
  }

  public void processMedicationRequests(Bundle medicationRequestBundle) {
    List<MedicationRequest> medicationRequestList = Utilities.getResourcesOfTypeFromBundle(MedicationRequest.class, medicationRequestBundle);
    if (!medicationRequestList.isEmpty()) {
      logger.info("r4/FhirBundleProcessor::processMedicationRequests: MedicationRequest(s) found");
      verifyDeidentifiedResources(medicationRequestBundle);

      for (MedicationRequest medicationRequest : medicationRequestList) {
        if (idInSelectionsList(medicationRequest.getId())) {
          List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(medicationRequest.getMedicationCodeableConcept(), medicationRequest.getInsurance(), null);
          buildExecutionContexts(criteriaList, (Patient) medicationRequest.getSubject().getResource(), "medication_request", medicationRequest);
        }
      }
    }
  }

  public void processMedicationDispenses(Bundle medicationDispenseBundle) {
    List<MedicationDispense> medicationDispenseList = Utilities.getResourcesOfTypeFromBundle(MedicationDispense.class, medicationDispenseBundle);
    if (!medicationDispenseList.isEmpty()) {
      logger.info("r4/FhirBundleProcessor::processMedicationDispenses: MedicationDispense(s) found");
      verifyDeidentifiedResources(medicationDispenseBundle);

      List<Organization> payorList = Utilities.getResourcesOfTypeFromBundle(Organization.class,
          medicationDispenseBundle);

      for (MedicationDispense medicationDispense : medicationDispenseList) {
        if (idInSelectionsList(medicationDispense.getId())) {
          List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(medicationDispense.getMedicationCodeableConcept(), null, payorList);
          buildExecutionContexts(criteriaList, (Patient) medicationDispense.getSubject().getResource(), "medication_dispense", medicationDispense);
        }
      }
    }
  }

  public void processServiceRequests(Bundle serviceRequestBundle) {
    List<ServiceRequest> serviceRequestList = Utilities.getResourcesOfTypeFromBundle(ServiceRequest.class, serviceRequestBundle);
    if (!serviceRequestList.isEmpty()) {
      logger.info("r4/FhirBundleProcessor::processServiceRequests: ServiceRequest(s) found");
      verifyDeidentifiedResources(serviceRequestBundle);

      for (ServiceRequest serviceRequest : serviceRequestList) {
        if (idInSelectionsList(serviceRequest.getId())) {
          List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(serviceRequest.getCode(), serviceRequest.getInsurance(), null);
          buildExecutionContexts(criteriaList, (Patient) serviceRequest.getSubject().getResource(), "service_request", serviceRequest);
        }
      }
    }
  }

  public void processOrderSelectMedicationStatements(Bundle medicationRequestBundle, Bundle medicationStatementBundle) {
    List<MedicationRequest> medicationRequestList = Utilities.getResourcesOfTypeFromBundle(MedicationRequest.class, medicationRequestBundle);
    List<MedicationStatement> medicationStatementList = Utilities.getResourcesOfTypeFromBundle(MedicationStatement.class, medicationStatementBundle);

    if (!medicationRequestList.isEmpty()) {
      logger.info("r4/FhirBundleProcessor::processOrderSelectMedicationStatements: MedicationRequests(s) found");
      verifyDeidentifiedResources(medicationRequestBundle);
      verifyDeidentifiedResources(medicationStatementBundle);

      // process each of the MedicationRequests
      for (MedicationRequest medicationRequest : medicationRequestList) {
        if (idInSelectionsList(medicationRequest.getId())) {

          // run on each of the MedicationStatements
          for (MedicationStatement medicationStatement : medicationStatementList) {
            logger.info("r4/FhirBundleProcessor::processOrderSelectMedicationStatements: MedicationStatement found: " + medicationStatement.getId());

            List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(medicationRequest.getMedicationCodeableConcept(), medicationRequest.getInsurance(), null);

            HashMap<String, Resource> cqlParams = new HashMap<>();
            cqlParams.put("Patient", (Patient) medicationRequest.getSubject().getResource());
            cqlParams.put("medication_request", medicationRequest);
            cqlParams.put("medication_statement", medicationStatement);

            buildExecutionContexts(criteriaList, cqlParams);
          }
        }
      }
    }
  }

  private List<CoverageRequirementRuleCriteria> createCriteriaList(CodeableConcept codeableConcept, List<Reference> insurance, List<Organization> payorList) {
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
        // Remove null coverages that may not have resolved.
        coverages = coverages.stream().filter(coverage -> coverage != null).collect(Collectors.toList());
        payors = Utilities.getPayors(coverages);
      } else if (payorList != null) {
        payors = payorList;
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
    HashMap<String, Resource> cqlParams = new HashMap<>();
    cqlParams.put("Patient", patient);
    cqlParams.put(requestType, request);

    buildExecutionContexts(criteriaList, cqlParams);
  }

  private void buildExecutionContexts(List<CoverageRequirementRuleCriteria> criteriaList, HashMap<String, Resource> cqlParams) {

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
          result.setContext(CqlExecutionContextBuilder.getExecutionContext(cqlRule, cqlParams, baseUrl));
          result.setDeidentifiedResourceContainsPhi(deidentifiedResourcesContainPhi);
          results.add(result);
        } catch (Exception e) {
          logger.info("r4/FhirBundleProcessor::buildExecutionContexts: failed processing cql bundle: " + e.getMessage());
        }
      }
    }
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
