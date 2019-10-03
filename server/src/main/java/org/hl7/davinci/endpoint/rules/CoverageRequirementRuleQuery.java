package org.hl7.davinci.endpoint.rules;

import java.util.List;
import java.util.logging.Logger;

import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;

public class CoverageRequirementRuleQuery {
  private static Logger logger = Logger.getLogger(Application.class.getName());

  private List<CoverageRequirementRule> response;
  private CoverageRequirementRuleCriteria criteria;
  private CoverageRequirementRuleFinder finder;

  public CoverageRequirementRuleQuery(CoverageRequirementRuleFinder finder, CoverageRequirementRuleCriteria criteria) {
    this.finder = finder;
    this.criteria = criteria;
  }

  public void execute() {
    logger.info("CoverageRequrementRuleQuery.execute(" + criteria.toString());
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
