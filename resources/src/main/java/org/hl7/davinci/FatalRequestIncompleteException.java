package org.hl7.davinci;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class FatalRequestIncompleteException extends RuntimeException {
  public FatalRequestIncompleteException(String message) {
    super(message);
  }
}