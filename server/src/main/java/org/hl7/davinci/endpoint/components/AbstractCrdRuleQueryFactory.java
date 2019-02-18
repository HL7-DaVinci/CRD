package org.hl7.davinci.endpoint.components;

import org.hl7.davinci.endpoint.database.CoverageRequirementRuleCriteria;

abstract public class AbstractCrdRuleQueryFactory {
  abstract public AbstractCrdRuleQuery create(CoverageRequirementRuleCriteria criteria);
}
