package org.hl7.davinci.endpoint.database;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

// patientAgeRangeLow, patientAgeRangeHigh,
// patientGender, patientPlanId, equipmentCode,
// noAuthNeeded, infoLink
@Entity
@Table(name = "coverage_requirement_rules")
public class CoverageRequirementRule {
  // the order in which these fields appear will be the order
  // in which they are organized in the data table.
  // The desired configuration is:
  // ID | AGELOW | AGEHIGH | GENDER | CODE |
  // SYSTEM | PATIENTADDRESS | PRACTITIONERADDRESS | DOC REQ | LINK |
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false, nullable = false)
  private long id;

  //    The following fields describe the rule
  @Column(name = "age_range_low", nullable = false)
  private int ageRangeLow;

  @Column(name = "age_range_high", nullable = false)
  private int ageRangeHigh;

  @Column(name = "gender_code", nullable = true)
  private Character genderCode;

  @Column(name = "equipment_code", nullable = false)
  private String equipmentCode;

  @Column(name = "code_system", nullable = false)
  private String codeSystem;

  @Column(name = "patient_address_state", nullable = true, length = 2)
  private String patientAddressState;

  @Column(name = "provider_address_state", nullable = true, length = 2)
  private String providerAddressState;

  @Column(name = "cql", nullable = false, length = 4000)
  private String cql;



  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getCql() {
    return cql;
  }

  public void setCql(String cql) {
    this.cql = cql;
  }

  public int getAgeRangeLow() {
    return ageRangeLow;
  }

  public void setAgeRangeLow(int ageRangeLow) {
    this.ageRangeLow = ageRangeLow;
  }

  public int getAgeRangeHigh() {
    return ageRangeHigh;
  }

  public void setAgeRangeHigh(int ageRangeHigh) {
    this.ageRangeHigh = ageRangeHigh;
  }

  public Character getGenderCode() {
    return genderCode;
  }

  public void setGenderCode(Character genderCode) {
    this.genderCode = genderCode;
  }

  public String getEquipmentCode() {
    return equipmentCode;
  }

  public void setEquipmentCode(String equipmentCode) {
    this.equipmentCode = equipmentCode;
  }

  public String getCodeSystem() {
    return codeSystem;
  }

  public void setCodeSystem(String codeSystem) {
    this.codeSystem = codeSystem;
  }

  public String getPatientAddressState() {
    return patientAddressState;
  }

  public void setPatientAddressState(String patientAddressState) {
    this.patientAddressState = patientAddressState;
  }

  public String getProviderAddressState() {
    return providerAddressState;
  }

  public void setProviderAddressState(String providerAddressState) {
    this.providerAddressState = providerAddressState;
  }

  @Override
  public String toString() {
    return String.format("(row id: %d) Rule [equipment_code: %s, code_system %s, age_range_low %d, age_range_high: %d"
        + ", gender_code: %s, patient_address_state: %s, practitioner_address_state] Cql: %s", id, equipmentCode, codeSystem, ageRangeLow,
        ageRangeHigh, genderCode, patientAddressState, providerAddressState, cql);
  }

  public CoverageRequirementRule() {}


  /**
   * Returns the name of the fields for dynamic generation of html files.
   *
   * @return the list of strings of all the member variables of this class
   */
  public static List<String> getFields() {
    List<String> fieldList = new ArrayList<>();
    for (Field field : CoverageRequirementRule.class.getDeclaredFields()) {
      String name = field.getName();
      fieldList.add(name);
    }
    return fieldList;
  }
}
