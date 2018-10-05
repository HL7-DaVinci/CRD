package org.hl7.davinci;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.davinci.stu3.FhirComponents;

public interface FhirComponentT {

  FhirContext getFhirContext();

  IParser getJsonParser();
}
