package org.hl7.davinci;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.EligibilityResponse;

@ResourceDef(name="DaVinciEligibilityResponse", profile="http://.../DaVinciPatient")
public class DaVinciEligibilityResponse extends EligibilityResponse {
    
}
