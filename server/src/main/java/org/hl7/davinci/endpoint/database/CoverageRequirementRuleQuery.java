package org.hl7.davinci.endpoint.database;

import org.hl7.davinci.endpoint.components.AbstractCrdRuleQuery;

import java.util.ArrayList;
import java.util.List;

public class CoverageRequirementRuleQuery extends AbstractCrdRuleQuery {

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

  public List<String> getCql() {
    List<String> cqlList = new ArrayList<>();
    this.execute();
    for (CoverageRequirementRule rule: this.getResponse()) {
      cqlList.add(rule.getCql());
    }
    return cqlList;
  }
}
