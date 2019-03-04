package org.hl7.davinci.endpoint.rules;

import java.util.List;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;

public class CoverageRequirementRuleQuery {

  private List<CoverageRequirementRule> response;
  private CoverageRequirementRuleCriteria criteria;
  private CoverageRequirementRuleFinder finder;

  public CoverageRequirementRuleQuery(CoverageRequirementRuleFinder finder, CoverageRequirementRuleCriteria criteria) {
    this.finder = finder;
    this.criteria = criteria;
  }

  public void execute() {
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
