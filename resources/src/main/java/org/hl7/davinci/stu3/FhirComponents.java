package org.hl7.davinci.stu3;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.davinci.stu3.fhirresources.DaVinciDeviceRequest;
import org.hl7.davinci.stu3.fhirresources.DaVinciMedicationRequest;

/**
 * Build some expensive objects here so we can reuse them.
 */
public class FhirComponents {

  private static FhirComponents single_instance = null;
  private FhirContext fhirContext;
  private IParser jsonParser;

  private FhirComponents() {
    fhirContext = FhirContext.forDstu3();
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

  /**
   * Gets the current FhirComponents instance.
   *
   * @return the instance
   */
  public static FhirComponents getInstance() {
    if (single_instance == null) {
      single_instance = new FhirComponents();
    }
    return single_instance;
  }

  public FhirContext getFhirContext() {
    return fhirContext;
  }

  public IParser getJsonParser() {
    return jsonParser;
  }
}
