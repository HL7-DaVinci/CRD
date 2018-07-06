package org.hl7.davinci;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import org.hl7.fhir.r4.model.*;

import java.util.List;

/**
 * A concrete implementation of the coverage-requirements-discovery operation.
 */
public class CoverageRequirementsDiscoveryOperation implements CoverageRequirementsDiscoveryOperationInterface {

    @Operation(name="$coverage-requirements-discovery", idempotent=true)
    public Parameters coverageRequirementsDiscovery(
            @OperationParam(name="request") Parameters.ParametersParameterComponent request,
            @OperationParam(name="endpoint") Endpoint endpoint,
            @OperationParam(name="requestQualification") CodeableConcept requestQualification
            ) {
        System.out.println("coverageRequirementsDiscovery: start");

        Parameters retVal = new Parameters();

        EligibilityRequest eligibilityRequest = null;
        Patient patient = null;
        Coverage coverage = null;
        Practitioner provider = null;
        Organization insurer = null;
        Location facility = null;
        // supportingInformation
        // serviceInformation

        // grab the list of parameters
        List<Parameters.ParametersParameterComponent> paramList = request.getPart();

        // pull each of the parameters from the list
        for (Parameters.ParametersParameterComponent part : paramList) {
            switch (part.getName()) {
                case "eligibilityrequest":
                    System.out.println("CRD: got eligibilityRequest");
                    eligibilityRequest = (EligibilityRequest) part.getResource();
                    break;
                case "patient":
                    System.out.println("CRD: got patient");
                    patient = (Patient) part.getResource();
                    break;
                case "coverage":
                    System.out.println("CRD: got coverage");
                    coverage = (Coverage) part.getResource();
                    break;
                case "provider":
                    System.out.println("CRD: got provider");
                    provider = (Practitioner) part.getResource();
                    break;
                case "insurer":
                    System.out.println("CRD: got insurer");
                    insurer = (Organization) part.getResource();
                    break;
                case "facility":
                    System.out.println("CRD: got facility");
                    facility = (Location) part.getResource();
                    break;
                case "patientContext":
                    ResourceType patientContextType = part.getResource().getResourceType();
                    switch (patientContextType) {
                        case Condition:
                            System.out.println("CRD: got request.patientContext of type Condition");
                            break;
                        case Device:
                            System.out.println("CRD: got request.patientContext of type Device");
                            break;
                        case Procedure:
                            System.out.println("CRD: got request.patientContext of type Procedure");
                            break;
                        case MedicationStatement:
                            System.out.println("CRD: got request.patientContext of type MedicationStatement");
                            break;
                        case HealthcareService:
                            System.out.println("CRD: got request.patientContext of type HealthcareService");
                            break;
                        default:
                            System.out.println("Warning: unexpected request.patientContext type");
                            break;
                    }
                    break;
                case "serviceInformationReference":
                    ResourceType serviceInformationReferenceType = part.getResource().getResourceType();
                    switch (serviceInformationReferenceType) {
                    case Procedure:
                        System.out.println("CRD: got request.serviceInformationReferenceType of type Procedure");
                        break;
                    case HealthcareService:
                        System.out.println("CRD: got request.serviceInformationReferenceType of type HealthcareService");
                        break;
                    case ServiceRequest:
                        System.out.println("CRD: got request.serviceInformationReferenceType of type ServiceRequest");
                        break;
                    case MedicationRequest:
                        System.out.println("CRD: got request.serviceInformationReferenceType of type MedicationRequest");
                        break;
                    case Medication:
                        System.out.println("CRD: got request.serviceInformationReferenceType of type Medication");
                        break;
                    case Device:
                        System.out.println("CRD: got request.serviceInformationReferenceType of type Device");
                        break;
                    case DeviceRequest:
                        System.out.println("CRD: got request.serviceInformationReferenceType of type DeviceRequest");
                        break;
                    default:
                        System.out.println("Warning: unexpected request.serviceInformationReferenceType type");
                        break;
                }
                    break;
                default:
                    System.out.println("Warning: unexpected parameter part: " + part.getName());
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
            System.out.println("ERROR: required information missing!");
            return retVal;
        }

        // print out the patient name
        System.out.println("CRD: Patient Name: " + patient.getName().get(0).getText());

        // start building the response
        EligibilityResponse eligibilityResponse = new EligibilityResponse();
        eligibilityResponse.setDisposition("this is a test");

        Endpoint finalEndPoint = new Endpoint();
        finalEndPoint.setAddress("http://www.mitre.org");

        Parameters.ParametersParameterComponent response = retVal.addParameter();
        response.setName("response");

        response.addPart().setName("eligibilityResponse").setResource(eligibilityResponse);
        response.addPart().setName("requestProvider").setResource(provider);
        response.addPart().setName("request").setResource(eligibilityRequest);
        response.addPart().setName("insurer").setResource(insurer);
        response.addPart().setName("coverage").setResource(coverage);

        // add a few service resources to the parameters
        Procedure procedure = new Procedure();
        procedure.setId("procedure-1");
        Device device = new Device();
        device.setModel("LMNOP678");
        response.addPart().setName("service").setResource(procedure);
        response.addPart().setName("service").setResource(device);


        if (finalEndPoint != null) {
            response.addPart().setName("endPoint").setResource(finalEndPoint);
        }

        System.out.println("coverageRequirementsDiscovery: end");
        return retVal;
    }

    boolean nullCheck(Resource obj, String objName) {
        if (obj == null) {
            System.out.println(objName + " is null");
            return true;
        } else {
            return false;
        }
    }
}
