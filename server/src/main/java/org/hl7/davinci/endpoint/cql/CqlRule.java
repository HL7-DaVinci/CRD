package org.hl7.davinci.endpoint.cql;


public class CqlRule {

  private String infoLink;
  private Boolean noAuthNeeded;
  private int ageRangeLow;
  private int ageRangeHigh;
  private Character genderCode;
  private String equipmentCode;
  private String codeSystem;
  private String patientAddressState;
  private String providerAddressState;

  public String getInfoLink() { return infoLink; }

  public CqlRule setInfoLink(String infoLink) {
    this.infoLink = infoLink;
    return this;
  }

  public boolean getNoAuthNeeded() { return noAuthNeeded; }

  public CqlRule setNoAuthNeeded(Boolean noAuthNeeded) {
    this.noAuthNeeded = noAuthNeeded;
    return this;
  }

  public int getAgeRangeLow() { return ageRangeLow; }

  public CqlRule setAgeRangeLow(int ageRangeLow) {
    this.ageRangeLow = ageRangeLow;
    return this;
  }

  public int getAgeRangeHigh() { return ageRangeHigh; }

  public CqlRule setAgeRangeHigh(int ageRangeHigh) {
    this.ageRangeHigh = ageRangeHigh;
    return this;
  }

  public Character getGenderCode() { return genderCode; }

  public CqlRule setGenderCode(Character genderCode) {
    this.genderCode = genderCode;
    return this;
  }

  public String getEquipmentCode() { return equipmentCode; }

  public CqlRule setEquipmentCode(String equipmentCode) {
    this.equipmentCode = equipmentCode;
    return this;
  }

  public String getCodeSystem() { return codeSystem; }

  public CqlRule setCodeSystem(String codeSystem) {
    this.codeSystem = codeSystem;
    return this;
  }

  public String getPatientAddressState() { return patientAddressState; }

  public CqlRule setPatientAddressState(String patientAddressState) {
    this.patientAddressState = patientAddressState;
    return this;
  }

  public String getProviderAddressState() { return providerAddressState; }

  public CqlRule setProviderAddressState(String providerAddressState) {
    this.providerAddressState = providerAddressState;
    return this;
  }

  @Override
  public String toString() {
    return String.format("Rule [equipment_code: %s, code_system %s, age_range_low %d, age_range_high: %d"
            + ", gender_code: %s, patient_address_state: %s, practitioner_address_state] Outcome: [no_auth_needed: %s, info_link %s]", equipmentCode, codeSystem, ageRangeLow,
        ageRangeHigh, genderCode, patientAddressState, providerAddressState, noAuthNeeded, infoLink);
  }
}
