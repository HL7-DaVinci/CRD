package org.hl7.davinci.endpoint.vsac.errors;

public class VSACValueSetNotFoundException extends VSACException {

  private static final long serialVersionUID = 1L;

  public VSACValueSetNotFoundException(String oid) {
    super("ValueSet " + oid + " Not Found.");
  }
}