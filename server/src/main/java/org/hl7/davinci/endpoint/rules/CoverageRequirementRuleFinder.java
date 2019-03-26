package org.hl7.davinci.endpoint.rules;

import java.util.List;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;


public interface CoverageRequirementRuleFinder {

  public List<CoverageRequirementRule> findRules(CoverageRequirementRuleCriteria criteria);

  public List<CoverageRequirementRule> findAll();
}
