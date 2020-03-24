package org.hl7.davinci.endpoint.vsac.errors;

public class VSACException extends Exception {

  private static final long serialVersionUID = 1L;

  public VSACException() {
  }

  public VSACException(String message) {
    super(message);
  }

  public VSACException(Throwable cause) {
    super(cause);
  }

  public VSACException(String message, Throwable cause) {
    super(message, cause);
  }
  
}

