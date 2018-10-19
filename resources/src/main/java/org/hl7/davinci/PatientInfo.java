package org.hl7.davinci;

public class PatientInfo {
  private Character patientGenderCode = null;
  private String patientAddressState = null;
  private Integer patientAge = null;

  public PatientInfo(Character patientGenderCode, String patientAddressState,
      Integer patientAge) {
    this.patientGenderCode = patientGenderCode;
    this.patientAddressState = patientAddressState;
    this.patientAge = patientAge;
  }

  public Character getPatientGenderCode() {
    return patientGenderCode;
  }

  public void setPatientGenderCode(Character patientGenderCode) {
    this.patientGenderCode = patientGenderCode;
  }

  public String getPatientAddressState() {
    return patientAddressState;
  }

  public void setPatientAddressState(String patientAddressState) {
    this.patientAddressState = patientAddressState;
  }

  public Integer getPatientAge() {
    return patientAge;
  }

  public void setPatientAge(Integer patientAge) {
    this.patientAge = patientAge;
  }
}
