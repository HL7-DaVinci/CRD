package org.hl7.davinci.endpoint.database;

import java.util.List;

public class CoverageRequirementRuleQuery {

  private List<CoverageRequirementRule> response;
  private Criteria criteria;
  private CoverageRequirementRuleFinder finder;

  public CoverageRequirementRuleQuery(CoverageRequirementRuleFinder finder) {
    this.finder = finder;
    this.criteria = new Criteria();
  }

  public void execute(){
    response = finder.findRules(criteria);
  }

  public List<CoverageRequirementRule> getResponse() {
    return response;
  }

  public void setResponse(
      List<CoverageRequirementRule> response) {
    this.response = response;
  }

  public Criteria getCriteria() {
    return criteria;
  }

  public void setCriteria(Criteria criteria) {
    this.criteria = criteria;
  }

  public class Criteria {

    private int age;
    private char genderCode;
    private String equipmentCode;
    private String codeSystem;
    private String patientAddressState;
    private String providerAddressState;

    public Criteria(){}

    public String toString() {
      return String.format(
          "age=%d, genderCode=%c, equipmentCode=%s, codeSystem=%s, patientAddressState=%s, providerAddressState=%s",
          age, genderCode, equipmentCode, codeSystem, patientAddressState, providerAddressState);

    }

    public Criteria setAge(int age) {
      this.age = age;
      return this;
    }

    public Criteria setGenderCode(char genderCode) {
      this.genderCode = genderCode;
      return this;
    }

    public Criteria setEquipmentCode(String equipmentCode) {
      this.equipmentCode = equipmentCode;
      return this;
    }

    public Criteria setCodeSystem(String codeSystem) {
      this.codeSystem = codeSystem;
      return this;
    }

    public Criteria setPatientAddressState(String patientAddressState) {
      this.patientAddressState = patientAddressState;
      return this;
    }

    public Criteria setProviderAddressState(String providerAddressState) {
      this.providerAddressState = providerAddressState;
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
  }
}
