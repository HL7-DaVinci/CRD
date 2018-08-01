package org.hl7.davinci;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;

import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.EligibilityRequest;
import org.hl7.fhir.r4.model.EligibilityResponse;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * A concrete implementation of the coverage-requirements-discovery operation.
 */
public class CoverageRequirementsDiscoveryOperationHardCodedResponse
    implements CoverageRequirementsDiscoveryOperationInterface {
  private static final Logger logger = LoggerFactory
      .getLogger(CoverageRequirementsDiscoveryOperationHardCodedResponse.class);

  /**
   * Submits the CRD request.
   * @param request the request to be submitted
   * @param endpoint the endpoint to submit the request to
   * @param requestQualification what kind of request it is
   * @return the parameters returned from the server
   */
  @Operation(name = "$coverage-requirements-discovery", idempotent = true)
  public Parameters coverageRequirementsDiscovery(
      @OperationParam(name = "request") Parameters.ParametersParameterComponent request,
      @OperationParam(name = "endpoint") Endpoint endpoint,
      @OperationParam(name = "requestQualification") CodeableConcept requestQualification
  ) {
    logger.debug("coverageRequirementsDiscovery: start");

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
          logger.debug("CRD: got eligibilityRequest");
          eligibilityRequest = (EligibilityRequest) part.getResource();
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
              logger.debug("CRD: got request.serviceInformationReferenceType "
                  + "of type HealthcareService");
              break;
            case ServiceRequest:
              logger.debug("CRD: got request.serviceInformationReferenceType "
                  + "of type ServiceRequest");
              break;
            case MedicationRequest:
              logger.debug("CRD: got request.serviceInformationReferenceType "
                  + "of type MedicationRequest");
              break;
            case Medication:
              logger.debug("CRD: got request.serviceInformationReferenceType of type Medication");
              break;
            case Device:
              logger.debug("CRD: got request.serviceInformationReferenceType of type Device");
              break;
            case DeviceRequest:
              logger.debug("CRD: got request.serviceInformationReferenceType "
                  + "of type DeviceRequest");
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
    if (nullCheck(eligibilityRequest, "eligibilityRequest")
        || nullCheck(patient, "patient")
        || nullCheck(coverage, "coverage")
        || nullCheck(provider, "provider")
        || nullCheck(insurer, "insurer")) {
      logger.error("ERROR: required information missing!");
      return retVal;
    }

    // print out the patient name
    assert patient != null;
    if (patient.hasName()) {
      logger.debug("CRD: Patient Name: " + patient.getName().get(0).getText());
    } else {
      logger.debug("CRD: No Patient Name provided");
    }

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

    logger.debug("coverageRequirementsDiscovery: end");
    return retVal;
  }

  private boolean nullCheck(Resource obj, String objName) {
    if (obj == null) {
      logger.debug(objName + " is null");
      return true;
    } else {
      return false;
    }
  }
}
