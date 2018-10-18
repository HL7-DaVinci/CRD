package org.hl7.davinci.endpoint.database;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoverageRequirementRuleFinder {

  static final Logger logger =
      LoggerFactory.getLogger(CoverageRequirementRuleFinder.class);

  @Autowired
  DataRepository repository;

  public CoverageRequirementRuleFinder() {
  }

  /**
   * Find and return the relevant coverage rule in the database.
   *
   * @param criteria The search criteria object
   */
  public List<CoverageRequirementRule> findRules(CoverageRequirementRuleCriteria criteria) {
    List<CoverageRequirementRule> ruleList = repository.findRules(criteria);
    if (ruleList.size() == 0) {
      logger.debug("RuleFinder returned no results for query: " + criteria.toString());
    }
    return ruleList;
  }
}
