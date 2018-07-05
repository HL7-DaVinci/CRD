package org.hl7.davinci;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Parameters;

/**
 * The interface that defines the coverage-requirements-discovery.
 */
public interface CoverageRequirementsDiscoveryOperationInterface {

    @Operation(name="$coverage-requirements-discovery", idempotent=true)
    Parameters coverageRequirementsDiscovery(
            @OperationParam(name="request") Parameters request,
            @OperationParam(name="endpoint") Endpoint endpoint,
            @OperationParam(name="requestQualification") CodeableConcept requestQualification
    );

}

/*
IN
1       request.eligibilityrequest      EligibilityRequest
1       request.patient                 Patient
1       request.coverage                Coverage
1       request.provider                Practitioner
1       request.insurer                 Organization
0..1    request.facility                Location
0..*    request.supportingInformation   Condition | Device | Procedure | MedicationStatement | HealthcareServices
0..*    request.serviceInformation      Procedure | HealthcareService | ServiceRequest | MedicationRequest | Medication | Device | DeviceRequest

0..1    endpoint                        Endpoint

0..*    requestQualification            CodeableConcept


OUT
1       response.eligibilityResponse    EligibilityResponse
1       response.requestProvider        Practitioner
1       response.request                EligibilityRequest
1       response.insurer                Organization
1       response.coverage               Coverage
0..*    response.service                Procedure | HealthcareService | ServiceRequest | MedicationRequest | Medication | Device | DeviceRequest
0..1    response.endPoint               Endpoint
*/