package org.hl7.davinci.endpoint.components;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.springframework.stereotype.Component;

/**
 * Build some expensive objects here so we can reuse them.
 */
@Component
public class FhirComponents {
  private FhirContext fhirContext;
  private IParser jsonParser;

  public FhirComponents() {
    this.fhirContext = FhirContext.forR4();
    this.jsonParser = fhirContext.newJsonParser();
  }

  public FhirContext getFhirContext() {
    return fhirContext;
  }

  public IParser getJsonParser() {
    return jsonParser;
  }
}
