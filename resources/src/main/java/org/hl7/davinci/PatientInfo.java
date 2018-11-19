package org.hl7.davinci;

public class PatientInfo {
  private Character patientGenderCode = null;
  private String patientAddressState = null;
  private Integer patientAge = null;
  private String patientId = null;

  /**
   * Constructor for the patientInfo class.
   * @param patientGenderCode the gender code, a single character
   * @param patientAddressState the two letter code for a patients state
   * @param patientAge the ingteger value of the patients age
   */
  public PatientInfo(Character patientGenderCode, String patientAddressState,
      Integer patientAge, String patientId) {
    this.patientGenderCode = patientGenderCode;
    this.patientAddressState = patientAddressState;
    this.patientAge = patientAge;
    this.patientId = patientId;
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

  public String getPatientId() { return patientId; }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

}

