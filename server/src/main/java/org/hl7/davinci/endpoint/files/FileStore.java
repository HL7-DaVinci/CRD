package org.hl7.davinci.endpoint.files;

import java.util.List;

import org.hl7.davinci.endpoint.cql.bundle.CqlRule;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.database.RuleMapping;

public interface FileStore {

  void update();

  CqlRule getCqlRule(String topic, String fhirVersion);

  //TODO: byte[] getFile(topic, fileName, fhirVersion);

  // from RuleFinder
  List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria);
  List<RuleMapping> findAll();
}
