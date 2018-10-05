package org.hl7.davinci.r4;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.davinci.FhirComponentT;

/**
 * Build some expensive objects here so we can reuse them.
 */
public class FhirComponents implements FhirComponentT {
  private static FhirContext fhirContext = FhirContext.forR4();
  private IParser jsonParser;

  public FhirComponents() {
    jsonParser = fhirContext.newJsonParser();
  }

  public FhirContext getFhirContext() {
    return fhirContext;
  }

  public IParser getJsonParser() {
    return jsonParser;
  }

}
