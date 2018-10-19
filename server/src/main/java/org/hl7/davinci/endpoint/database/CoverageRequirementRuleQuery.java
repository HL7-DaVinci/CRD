package org.hl7.davinci.endpoint.database;

import java.util.List;

public class CoverageRequirementRuleQuery {

  private List<CoverageRequirementRule> response;
  private CoverageRequirementRuleCriteria criteria;
  private CoverageRequirementRuleFinder finder;

  public CoverageRequirementRuleQuery(CoverageRequirementRuleFinder finder) {
    this.finder = finder;
    this.criteria = new CoverageRequirementRuleCriteria();
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

  public CoverageRequirementRuleCriteria getCriteria() {
    return criteria;
  }

  public void setCriteria(CoverageRequirementRuleCriteria criteria) {
    this.criteria = criteria;
  }


}
