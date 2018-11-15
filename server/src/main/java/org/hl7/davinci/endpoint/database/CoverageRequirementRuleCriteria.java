package org.hl7.davinci.endpoint.database;

public class CoverageRequirementRuleCriteria {

  private int age;
  private char genderCode;
  private String equipmentCode;
  private String codeSystem;
  private String patientAddressState;
  private String providerAddressState;
  private String patientId;

  public CoverageRequirementRuleCriteria(){}

  /**
   * Converts the coverage requirement rule to a string.
   * @return the formatted string representation of the object
   */
  public String toString() {
    return String.format(
        "age=%d, genderCode=%c, equipmentCode=%s, codeSystem=%s, patientAddressState=%s, providerAddressState=%s, patientId=%s",
        age, genderCode, equipmentCode, codeSystem, patientAddressState, providerAddressState, patientId);

  }

  public CoverageRequirementRuleCriteria setAge(int age) {
    this.age = age;
    return this;
  }

  public CoverageRequirementRuleCriteria setGenderCode(char genderCode) {
    this.genderCode = genderCode;
    return this;
  }

  public CoverageRequirementRuleCriteria setEquipmentCode(String equipmentCode) {
    this.equipmentCode = equipmentCode;
    return this;
  }

  public CoverageRequirementRuleCriteria setCodeSystem(String codeSystem) {
    this.codeSystem = codeSystem;
    return this;
  }

  public CoverageRequirementRuleCriteria setPatientAddressState(String patientAddressState) {
    this.patientAddressState = patientAddressState;
    return this;
  }

  public CoverageRequirementRuleCriteria setProviderAddressState(String providerAddressState) {
    this.providerAddressState = providerAddressState;
    return this;
  }

  public CoverageRequirementRuleCriteria setPatientId(String patientId) {
    this.patientId = patientId;
    return this;
  }

  public int getAge() {
    return age;
  }

  public char getGenderCode() {
    return genderCode;
  }

  public String getEquipmentCode() {
    return equipmentCode;
  }

  public String getCodeSystem() {
    return codeSystem;
  }

  public String getPatientAddressState() {
    return patientAddressState;
  }

  public String getProviderAddressState() {
    return providerAddressState;
  }

  public String getPatientId() { return patientId; }
}