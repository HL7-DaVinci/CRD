package org.hl7.davinci;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public interface FhirComponentsT {

  enum Version {
    R4,
    STU3
  }


  FhirContext getFhirContext();

  IParser getJsonParser();

  Version getFhirVersion();
}
