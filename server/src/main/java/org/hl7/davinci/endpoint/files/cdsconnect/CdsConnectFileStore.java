package org.hl7.davinci.endpoint.files.cdsconnect;

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
@Profile("cdsConnect")
public class CdsConnectFileStore implements FileStore {

  static final Logger logger = LoggerFactory.getLogger(CdsConnectFileStore.class);

  public CdsConnectFileStore() {
    logger.info("Using CdsConnectFileStore");
  }

  public void update() {
    logger.info("CdsConnectFileStore::update()");
  }

  public CqlRule getCqlRule(String topic, String fhirVersion) {
    logger.info("CdsConnectFileStore::getCqlRule(): " + topic + "/" + fhirVersion);
    return new CqlRule();
  }

  public List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria) {
    logger.info("CdsConnectFileStore::findRules(): " + criteria.toString());
    return new ArrayList<>();
  }

  public List<RuleMapping> findAll() {
    logger.info("CdsConnectFileStore::findAll()");
    return new ArrayList<>();
  }
}