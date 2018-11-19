package org.hl7.davinci.r4;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.davinci.FhirComponentsT;

/**
 * Build some expensive objects here so we can reuse them.
 */
public class FhirComponents implements FhirComponentsT {

  private static FhirContext fhirContext;
  private static IParser jsonParser;
  private static Version fhirVersion;



  static {
    fhirContext = FhirContext.forR4();
    jsonParser = fhirContext.newJsonParser();
    fhirVersion = Version.R4;
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
