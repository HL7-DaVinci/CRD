package org.hl7.davinci.endpoint.rules;

import org.opencds.cqf.cql.engine.execution.Context;

public class CoverageRequirementRuleResult {

  private Context context;
  private CoverageRequirementRuleCriteria criteria;
  private String topic;
  private boolean deidentifiedResourceContainsPhi;

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

  public String getTopic() { return topic; }

  public CoverageRequirementRuleResult setTopic(String topic) {
    this.topic = topic;
    return this;
  }

  public boolean getDeidentifiedResourceContainsPhi() { return deidentifiedResourceContainsPhi; }

  public CoverageRequirementRuleResult setDeidentifiedResourceContainsPhi(boolean deidentifiedResourceContainsPhi) {
    this.deidentifiedResourceContainsPhi = deidentifiedResourceContainsPhi;
    return this;
  }
}
