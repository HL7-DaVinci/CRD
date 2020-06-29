package org.hl7.davinci;

import ca.uhn.fhir.parser.LenientErrorHandler;

public class SuppressParserErrorHandler extends LenientErrorHandler {
  @Override
  public void unknownElement(IParseLocation theLocation, String theElementName) {
    //do nothing to suppress the unknown element error
  }
}