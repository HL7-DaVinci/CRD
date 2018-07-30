package endpoint;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import endpoint.database.DMECoverageRequirementRule;
import endpoint.database.DMECoverageRequirementRuleFinder;
import endpoint.database.DataRepository;
import org.hl7.davinci.CoverageRequirementsDiscoveryOperationInterface;
import org.hl7.davinci.DaVinciEligibilityRequest;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An implementation of the coverage-requirements-discovery operation that finds a matching rule in the database.
 */
@Component
public class CoverageRequirementsDiscoveryOperationDatabaseVersion implements CoverageRequirementsDiscoveryOperationInterface {

    final static Logger logger = LoggerFactory.getLogger(CoverageRequirementsDiscoveryOperationDatabaseVersion.class);

    @Autowired
    DMECoverageRequirementRuleFinder ruleFinder;

//    public CoverageRequirementsDiscoveryOperationDatabaseVersion() {
//        this.ruleFinder = new DMECoverageRequirementRuleFinder();
//    }

    @Operation(name="$coverage-requirements-discovery", idempotent=true)
    public Parameters coverageRequirementsDiscovery(
            @OperationParam(name="request") Parameters.ParametersParameterComponent request,
            @OperationParam(name="endpoint") Endpoint endpoint,
            @OperationParam(name="requestQualification") CodeableConcept requestQualification
    ) {
        logger.debug("coverageRequirementsDiscovery: start");

        Parameters retVal = new Parameters();

        DaVinciEligibilityRequest eligibilityRequest = null;
        Patient patient = null;
        Coverage coverage = null;
        Practitioner provider = null;
        Organization insurer = null;
        Location facility = null;

        // grab the list of parameters
        List<Parameters.ParametersParameterComponent> paramList = request.getPart();

        // pull each of the parameters from the list
        for (Parameters.ParametersParameterComponent part : paramList) {
            switch (part.getName()) {
                case "eligibilityrequest":
                    logger.debug("CRD: got eligibilityRequest");
                    eligibilityRequest = (DaVinciEligibilityRequest) part.getResource();
                    break;
                case "patient":
                    logger.debug("CRD: got patient");
                    patient = (Patient) part.getResource();
                    break;
                case "coverage":
                    logger.debug("CRD: got coverage");
                    coverage = (Coverage) part.getResource();
                    break;
                case "provider":
                    logger.debug("CRD: got provider");
                    provider = (Practitioner) part.getResource();
                    break;
                case "insurer":
                    logger.debug("CRD: got insurer");
                    insurer = (Organization) part.getResource();
                    break;
                case "facility":
                    logger.debug("CRD: got facility");
                    facility = (Location) part.getResource();
                    break;
                case "patientContext":
                    ResourceType patientContextType = part.getResource().getResourceType();
                    switch (patientContextType) {
                        case Condition:
                            logger.debug("CRD: got request.patientContext of type Condition");
                            break;
                        case Device:
                            logger.debug("CRD: got request.patientContext of type Device");
                            break;
                        case Procedure:
                            logger.debug("CRD: got request.patientContext of type Procedure");
                            break;
                        case MedicationStatement:
                            logger.debug("CRD: got request.patientContext of type MedicationStatement");
                            break;
                        case HealthcareService:
                            logger.debug("CRD: got request.patientContext of type HealthcareService");
                            break;
                        default:
                            logger.warn("Warning: unexpected request.patientContext type");
                            break;
                    }
                    break;
                case "serviceInformationReference":
                    ResourceType serviceInformationReferenceType = part.getResource().getResourceType();
                    switch (serviceInformationReferenceType) {
                        case Procedure:
                            logger.debug("CRD: got request.serviceInformationReferenceType of type Procedure");
                            break;
                        case HealthcareService:
                            logger.debug("CRD: got request.serviceInformationReferenceType of type HealthcareService");
                            break;
                        case ServiceRequest:
                            logger.debug("CRD: got request.serviceInformationReferenceType of type ServiceRequest");
                            break;
                        case MedicationRequest:
                            logger.debug("CRD: got request.serviceInformationReferenceType of type MedicationRequest");
                            break;
                        case Medication:
                            logger.debug("CRD: got request.serviceInformationReferenceType of type Medication");
                            break;
                        case Device:
                            logger.debug("CRD: got request.serviceInformationReferenceType of type Device");
                            break;
                        case DeviceRequest:
                            logger.debug("CRD: got request.serviceInformationReferenceType of type DeviceRequest");
                            break;
                        default:
                            logger.warn("Warning: unexpected request.serviceInformationReferenceType type");
                            break;
                    }
                    break;
                default:
                    logger.warn("Warning: unexpected parameter part: " + part.getName());
                    break;
            }
        }

        // null check the required parameters
        // Note: if nothing is set in the reference (even if it was created) it will be null
        if ( nullCheck(eligibilityRequest, "eligibilityRequest")
                || nullCheck(patient, "patient")
                || nullCheck(coverage, "coverage")
                || nullCheck(provider, "provider")
                || nullCheck(insurer, "insurer") ) {
            logger.error("ERROR: required information missing!");
            return retVal;
        }

        //response should be populated with individual responses for each item in this list of requests, so we make a list of requests cpt codes
        List<String> cptCodes = new ArrayList<String>();
        List<DaVinciEligibilityRequest.ServiceInformation> serviceInformationList = eligibilityRequest.getServiceInformation();
        for (DaVinciEligibilityRequest.ServiceInformation serviceInformation: serviceInformationList){
            List<Coding> codings =  serviceInformation.getServiceRequestType().getCoding();
            for (Coding coding: codings){
                String system = coding.getSystem();
                if (system.equalsIgnoreCase("http://www.ama-assn.org/go/cpt")){ //currently only cpt codes supported
                    cptCodes.add(coding.getCode());
                    break;
                }
            }
        }

        //pull out the patient info
        LocalDate birthDate = toLocalDate(patient.getBirthDate());
        int age =  getAgeOnDateInYears(birthDate, LocalDate.now());
        Enumerations.AdministrativeGender gender = patient.getGender();


        //lookup the rule for each cpt code
        StringBuilder responseDescription = new StringBuilder();
        for (String cptCode: cptCodes) {
            DMECoverageRequirementRule rule = ruleFinder.findRule(age, gender, cptCode);
            if (rule == null) {
                responseDescription.append(cptCode + " = no information available\n");
            } else {
                responseDescription.append(cptCode + " = info: " + rule.getInfoLink() + " no auth needed:" + rule.getNoAuthNeeded()+"\n");
            }
        }


        // start building the response
        EligibilityResponse eligibilityResponse = new EligibilityResponse();
        eligibilityResponse.setDisposition(responseDescription.toString());

        Endpoint finalEndPoint = new Endpoint();
        finalEndPoint.setAddress("http://www.mitre.org");

        Parameters.ParametersParameterComponent response = retVal.addParameter();
        response.setName("response");

        response.addPart().setName("eligibilityResponse").setResource(eligibilityResponse);
        response.addPart().setName("requestProvider").setResource(provider);
        response.addPart().setName("request").setResource(eligibilityRequest);
        response.addPart().setName("insurer").setResource(insurer);
        response.addPart().setName("coverage").setResource(coverage);


        if (finalEndPoint != null) {
            response.addPart().setName("endPoint").setResource(finalEndPoint);
        }

        logger.debug("coverageRequirementsDiscovery: end");
        return retVal;
    }

    boolean nullCheck(Resource obj, String objName) {
        if (obj == null) {
            logger.debug(objName + " is null");
            return true;
        } else {
            return false;
        }
    }

    static int getAgeOnDateInYears(LocalDate birthDate, LocalDate dateOfAge){
        return Period.between(birthDate, dateOfAge).getYears();
    }

    static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
