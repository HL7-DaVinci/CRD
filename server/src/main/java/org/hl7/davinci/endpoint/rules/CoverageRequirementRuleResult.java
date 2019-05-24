package org.hl7.davinci.endpoint.rules;

import org.opencds.cqf.cql.execution.Context;

public class CoverageRequirementRuleResult {

  private Context context;
  private CoverageRequirementRuleCriteria criteria;

  public Context getContext() { return context; }

  public CoverageRequirementRuleResult setContext(Context context) {
    this.context = context;
    return this;
  }

  public CoverageRequirementRuleCriteria getCriteria() { return criteria; }

  public CoverageRequirementRuleResult setCriteria(CoverageRequirementRuleCriteria criteria) {
    this.criteria = criteria;
    return this;
  }
}
