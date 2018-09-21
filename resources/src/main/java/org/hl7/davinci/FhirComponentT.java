package org.hl7.davinci;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.davinci.stu3.FhirComponents;

public interface FhirComponentT {

  /**
   * Gets the current FhirComponents instance.
   *
   * @return the instance
   */
  FhirComponentT getInstance();

  FhirContext getFhirContext();

  IParser getJsonParser();
}
