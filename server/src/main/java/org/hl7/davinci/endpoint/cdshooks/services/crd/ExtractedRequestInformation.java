package org.hl7.davinci.endpoint.cdshooks.services.crd;

public class ExtractedRequestInformation {
  private int patientAge;
  private String patientGender;
  private String code;
  private String codeSystem;

  public int getPatientAge() {
    return patientAge;
  }

  public void setPatientAge(int patientAge) {
    this.patientAge = patientAge;
  }

  public String getPatientGender() {
    return patientGender.toUpperCase();
  }

  public void setPatientGender(String patientGender) {
    this.patientGender = patientGender;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getCodeSystem() {
    return codeSystem;
  }

  public void setCodeSystem(String codeSystem) {
    this.codeSystem = codeSystem;
  }
}
