package org.hl7.davinci;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public interface FhirComponentsT {

  FhirContext getFhirContext();

  IParser getJsonParser();

  String getFhirVersion();
}
