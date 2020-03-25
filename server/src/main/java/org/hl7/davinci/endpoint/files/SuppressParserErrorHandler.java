package org.hl7.davinci.endpoint.files;

import ca.uhn.fhir.parser.LenientErrorHandler;

public class SuppressParserErrorHandler extends LenientErrorHandler {
  @Override
  public void unknownElement(IParseLocation theLocation, String theElementName) {
    //do nothing to suppress the unknown element error
  }
}