package org.hl7.davinci.endpoint.files;

import java.util.List;
import java.io.File;

import org.hl7.davinci.endpoint.cql.bundle.CqlRule;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.database.RuleMapping;

public interface FileStore {

  void reload();

  CqlRule getCqlRule(String topic, String fhirVersion);

  FileResource getFile(String topic, String fileName, String fhirVersion, boolean convert);

  // from RuleFinder
  List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria);
  List<RuleMapping> findAll();
}
