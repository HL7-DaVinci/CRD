package org.hl7.davinci.endpoint.database;

import org.hl7.davinci.endpoint.components.AbstractCrdRuleQuery;
import org.hl7.davinci.endpoint.components.AbstractCrdRuleQueryFactory;

public class CoverageRequirementsRuleQueryFactory extends AbstractCrdRuleQueryFactory {

  private CoverageRequirementRuleFinder finder;

  public CoverageRequirementsRuleQueryFactory(CoverageRequirementRuleFinder finder) {
    this.finder = finder;
  }
  public AbstractCrdRuleQuery create(CoverageRequirementRuleCriteria criteria) {
    return new CoverageRequirementRuleQuery(this.finder, criteria);
  }
}
