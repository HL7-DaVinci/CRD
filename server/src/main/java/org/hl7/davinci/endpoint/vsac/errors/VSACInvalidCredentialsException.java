package org.hl7.davinci.endpoint.vsac.errors;

public class VSACInvalidCredentialsException extends VSACException {

  private static final long serialVersionUID = 1L;

  public VSACInvalidCredentialsException() {
    super("VSAC ULMS credentials are invalid.");
  }
  
}