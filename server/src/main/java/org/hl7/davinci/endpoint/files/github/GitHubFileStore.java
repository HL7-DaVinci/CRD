package org.hl7.davinci.endpoint.files.github;

import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.cql.bundle.CqlRule;
import org.hl7.davinci.endpoint.database.RuleMapping;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("gitHub")
public class GitHubFileStore implements FileStore {

  static final Logger logger = LoggerFactory.getLogger(GitHubFileStore.class);

  public GitHubFileStore() {
    logger.info("Using GitHubFileStore");
  }

  public void update() {
    logger.info("GitHubFileStore::update()");
  }

  public CqlRule getCqlRule(String topic, String fhirVersion) {
    logger.info("GitHubFileStore::getCqlRule(): " + topic + "/" + fhirVersion);
    return new CqlRule();
  }

  public List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria) {
    logger.info("GitHubFileStore::findRules(): " + criteria.toString());
    return new ArrayList<>();
  }

  public List<RuleMapping> findAll() {
    logger.info("GitHubFileStore::findAll()");
    return new ArrayList<>();
  }
}
