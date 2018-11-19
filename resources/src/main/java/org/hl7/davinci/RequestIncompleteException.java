package org.hl7.davinci;

public class RequestIncompleteException extends Exception {
  public RequestIncompleteException() { super(); }

  public RequestIncompleteException(String message) { super(message); }

  public RequestIncompleteException(String message, Throwable cause) { super(message, cause); }

  public RequestIncompleteException(Throwable cause) { super(cause); }
}