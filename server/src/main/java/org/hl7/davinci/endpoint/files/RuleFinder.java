package org.hl7.davinci.endpoint.files;

import java.util.ArrayList;
import java.util.List;

import org.hl7.davinci.endpoint.database.RuleMapping;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.database.RuleMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
//@Profile("localDb")
public class RuleFinder {

  static final Logger logger =
      LoggerFactory.getLogger(RuleFinder.class);

  @Autowired
  RuleMappingRepository ruleMappingRepository;

  public RuleFinder() {
    logger.info("Using RuleFinder");
  }

  /**
   * Find and retern the relevant coverage rules in the database.
   * @param criteria The search criteria object
   * @return List of matching RuleMapping entries
   */
  public List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria) {
    logger.info("RuleFinder::findRules(" + criteria.getQueryString() + ")");
    List<RuleMapping> ruleList = new ArrayList<>();
    if (ruleMappingRepository == null) {
      logger.warn("RuleFinder::findRules: the ruleMappingRepository is null");
      return ruleList;
    }
    for (RuleMapping rule : ruleMappingRepository.findRules(criteria)) {
      ruleList.add(rule);
    }
    if (ruleList.size() == 0) {
      logger.info("RuleFinder::findRules() returned no results for query: " + criteria.toString());
    }
    return ruleList;
  }

  public List<RuleMapping> findRules(String topic, String fhirVersion) {
    logger.info("RuleFinder::findRules(" + topic + ", " + fhirVersion + ")");
    List<RuleMapping> ruleList = new ArrayList<>();
    if (ruleMappingRepository == null) {
      logger.warn("RuleFinder::findRules: the ruleMappingRepository is null");
      return ruleList;
    }
    for (RuleMapping rule : ruleMappingRepository.findRules(topic, fhirVersion)) {
      ruleList.add(rule);
    }
    if (ruleList.size() == 0) {
      logger.info("RuleFinder::findRules() returned no results for topic: " + topic + "(" + fhirVersion + ")");
    }
    return ruleList;
  }

  /**
   * Find all of the lookup table rules in the database.
   * @return List of all RuleMapping entries
   */
  public List<RuleMapping> findAll() {
    logger.info("RuleFinder::findAll()");
    List<RuleMapping> ruleList = new ArrayList<>();
    for (RuleMapping rule : ruleMappingRepository.findAll()) {
      ruleList.add(rule);
    }
    if (ruleList.size() == 0) {
      logger.debug("RuleFinder returned no results for find all");
    }
    return ruleList;
  }
}
