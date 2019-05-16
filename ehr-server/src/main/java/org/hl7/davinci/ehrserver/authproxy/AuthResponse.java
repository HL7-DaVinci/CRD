package org.hl7.davinci.ehrserver.authproxy;


import java.util.UUID;

public class AuthResponse {
  private String launchId;

  AuthResponse() {
    this.launchId = UUID.randomUUID().toString();
  }

  public String getLaunchId() {
    return launchId;
  }



}
