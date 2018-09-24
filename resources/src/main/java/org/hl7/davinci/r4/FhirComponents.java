package org.hl7.davinci.r4;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.davinci.FhirComponentT;

/**
 * Build some expensive objects here so we can reuse them.
 */
public class FhirComponents implements FhirComponentT {
  private FhirContext fhirContext;
  private IParser jsonParser;

  public FhirComponents() {
    fhirContext = FhirContext.forR4();
    jsonParser = fhirContext.newJsonParser();
  }

  public FhirContext getFhirContext() {
    return fhirContext;
  }

  public IParser getJsonParser() {
    return jsonParser;
  }

}
