package org.hl7.davinci.endpoint.github;

import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

@Component
@Profile("gitHub")
public class GitHubRuleFinder implements CoverageRequirementRuleFinder {

  private static Logger logger = Logger.getLogger(Application.class.getName());

  @Autowired
  private GitHubConnection connection;

  public List<CoverageRequirementRule> findRules(CoverageRequirementRuleCriteria criteria) {
    logger.info("GitHubRuleFinder::findRules(" + criteria.getQueryString() + ")");
    return connection.getRules(criteria, true);
  }

  public List<CoverageRequirementRule> findAll() {
    logger.info("GitHubRuleFinder::findAll()");
    return connection.getAllRules();
  }

}
