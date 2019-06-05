package org.hl7.davinci.endpoint.rules;

import org.hl7.ShortNameMaps;

import java.util.ArrayList;
import java.util.List;

public class CoverageRequirementRuleCriteria {

  private String payor;
  private String code;
  private String codeSystem;

  public String getCode() {
    return code;
  }

  public CoverageRequirementRuleCriteria setCode(String code) {
    this.code = code;
    return this;
  }

  public String getCodeSystem() {
    return codeSystem;
  }

  public CoverageRequirementRuleCriteria setCodeSystem(String codeSystem) {
    this.codeSystem = codeSystem;
    return this;
  }

  public String getPayor() {
    return payor;
  }

  public CoverageRequirementRuleCriteria setPayor(String payor) {
    this.payor = payor;
    return this;
  }

  public CoverageRequirementRuleCriteria(){}

  /**
   * Converts the coverage requirement rule to a string.
   * @return the formatted string representation of the object
   */
  public String toString() {
    return String.format(
        "payor=%s, code=%s, codeSystem=%s", payor, code, codeSystem);

  }

  public static List<CoverageRequirementRuleCriteria> createQueriesFromStu3(List<org.hl7.fhir.dstu3.model.Coding> codings, List<org.hl7.fhir.dstu3.model.Organization> payors) {
    List<CoverageRequirementRuleCriteria> criteriaList = new ArrayList<>();
    for (org.hl7.fhir.dstu3.model.Coding coding : codings) {
      String code = coding.getCode();
      String codeSystem = coding.getSystem();
      for (org.hl7.fhir.dstu3.model.Organization payor : payors) {
        String payorName = payor.getName();
        CoverageRequirementRuleCriteria criteria = new CoverageRequirementRuleCriteria();
        criteria.setPayor(payorName).setCodeSystem(codeSystem).setCode(code);
        criteriaList.add(criteria);
      }
    }
    return criteriaList;
  }

  public static List<CoverageRequirementRuleCriteria> createQueriesFromR4(List<org.hl7.fhir.r4.model.Coding> codings, List<org.hl7.fhir.r4.model.Organization> payors) {
    List<CoverageRequirementRuleCriteria> criteriaList = new ArrayList<>();
    for (org.hl7.fhir.r4.model.Coding coding : codings) {
      String code = coding.getCode();
      String codeSystem = coding.getSystem();
      for (org.hl7.fhir.r4.model.Organization payor : payors) {
        String payorName = payor.getName();
        CoverageRequirementRuleCriteria criteria = new CoverageRequirementRuleCriteria();
        criteria.setPayor(payorName).setCodeSystem(codeSystem).setCode(code);
        criteriaList.add(criteria);
      }
    }
    return criteriaList;
  }

  public String getQueryString() {
    String payor = ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.inverse().get(this.getPayor());
    String codeSystem = ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.inverse().get(this.getCodeSystem());
    return String.format("%s/%s/%s", payor, codeSystem, this.getCode());
  }
}