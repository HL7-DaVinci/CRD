package org.hl7.davinci;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestfulDaVinciEligibilityResponseProvider{

    final static Logger logger = LoggerFactory.getLogger(RestfulDaVinciEligibilityResponseProvider.class);

    @Operation(name="$operation-test", idempotent=true)
    public DaVinciEligibilityResponse operationTest(
            @OperationParam(name="patient") DaVinciPatient patient
            ) {

        logger.debug("DAWG");

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
