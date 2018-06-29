package org.hl7.davinci;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;



public class RestfulDaVinciEligibilityResponseProvider{



    @Operation(name="$coverage-requirement-discovery", idempotent=true)
    public DaVinciEligibilityResponse coverageRequirementsDiscovery(
            @OperationParam(name="patient") DaVinciPatient patient
            ) {

        System.out.println("DAWG");

        DaVinciEligibilityResponse response = new DaVinciEligibilityResponse();
        response.setDisposition("sup dawg");

        return response;
//        return patient;

//        how to return multiple things... probably not needed
//        Parameters retVal = new Parameters();
//        retVal.addParameter().setName("echoPatient").setResource(patient);
//        return retVal;
    }
}
