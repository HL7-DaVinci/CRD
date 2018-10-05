package org.hl7.davinci.stu3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.davinci.FhirComponentT;
import org.hl7.davinci.stu3.fhirresources.DaVinciDeviceRequest;
import org.hl7.davinci.stu3.fhirresources.DaVinciMedicationRequest;

/**
 * Build some expensive objects here so we can reuse them.
 */
public class FhirComponents implements FhirComponentT {

  private static FhirContext fhirContext = FhirContext.forDstu3();
  private IParser jsonParser;

  public FhirComponents() {

    // This is needed to correctly cast an incoming DaVinciEligibilityRequest, url must match that
    // defined in the resource profile
    fhirContext.setDefaultTypeForProfile(
        "http://hl7.org/fhir/us/davinci-crd/STU3/StructureDefinition/profile-devicerequest-stu3",
        DaVinciDeviceRequest.class);
    fhirContext.setDefaultTypeForProfile(
        "http://hl7.org/fhir/us/davinci-crd/STU3/StructureDefinition/profile-medicationrequest-stu3",
        DaVinciMedicationRequest.class);

    jsonParser = fhirContext.newJsonParser();
  }

  public FhirContext getFhirContext() {
    return fhirContext;
  }

  public IParser getJsonParser() {
    return jsonParser;
  }
}
