package org.hl7.davinci.stu3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.stu3.fhirresources.DaVinciDeviceRequest;
import org.hl7.davinci.stu3.fhirresources.DaVinciMedicationRequest;

/**
 * Build some expensive objects here so we can reuse them.
 */
public class FhirComponents implements FhirComponentsT {

  private static FhirContext fhirContext;
  private static IParser jsonParser;
  private static FhirComponentsT.Version fhirVersion;

  static {
    fhirContext = FhirContext.forDstu3();
    jsonParser = fhirContext.newJsonParser();
    fhirVersion = Version.STU3;

    // This is needed to correctly cast an incoming DaVinciEligibilityRequest, url must match that
    // defined in the resource profile
    fhirContext.setDefaultTypeForProfile(
        "http://hl7.org/fhir/us/davinci-crd/STU3/StructureDefinition/profile-devicerequest-stu3",
        DaVinciDeviceRequest.class);
    fhirContext.setDefaultTypeForProfile(
        "http://hl7.org/fhir/us/davinci-crd/STU3/StructureDefinition/profile-medicationrequest-stu3",
        DaVinciMedicationRequest.class);
  }

  public FhirComponents() {

  }

  public FhirContext getFhirContext() {
    return fhirContext;
  }

  public IParser getJsonParser() {
    return jsonParser;
  }

  public Version getFhirVersion() {
    return fhirVersion;
  }
}
