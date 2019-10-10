package org.hl7.davinci.endpoint.database;

import java.util.ArrayList;
import java.util.List;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("localDb")
public class CoverageRequirementRuleFinderDatabase implements CoverageRequirementRuleFinder {

  static final Logger logger =
      LoggerFactory.getLogger(CoverageRequirementRuleFinderDatabase.class);

  @Autowired
  DataRepository repository;

  public CoverageRequirementRuleFinderDatabase() {
  }

  /**
   * Find and return the relevant coverage rule in the database.
   *
   * @param criteria The search criteria object
   */
  public List<CoverageRequirementRule> findRules(CoverageRequirementRuleCriteria criteria) {
    logger.info("CoverageRequirementRuleFinderDatabase::findRules(" + criteria.getQueryString() + ")");
    List<CoverageRequirementRule> ruleList = new ArrayList<>();
    for (CoverageRequirementRule rule :repository.findRules(criteria)){
      ruleList.add(rule);
    }
    if (ruleList.size() == 0) {
      logger.debug("RuleFinder returned no results for query: " + criteria.toString());
    }
    return ruleList;
  }

  /**
   * Find and return all coverage rules in the database.
   */
  public List<CoverageRequirementRule> findAll() {
    logger.info("CoverageRequirementRuleFinderDatabase::findAll()");
    List<CoverageRequirementRule> ruleList = new ArrayList<>();
    for (CoverageRequirementRule rule :repository.findAll()){
      ruleList.add(rule);
    }

    if (ruleList.size() == 0) {
      logger.debug("RuleFinder returned no results for find all");
    }

    return ruleList;
  }
}
