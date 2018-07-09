package org.hl7.davinci;
import ca.uhn.fhir.model.api.annotation.*;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;

// The "profile" argument decides which structure definition gets used for validation purposes.
@ResourceDef(name="Practitioner", profile="http://acme.org/blah")
public class DaVinciPractitioner extends Practitioner {


}

