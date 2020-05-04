package org.hl7.davinci.endpoint.files;

import java.util.List;

import org.hl7.davinci.endpoint.cql.CqlRule;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.database.RuleMapping;

public interface FileStore {

  void reload();
  void reinitializeVSACLoader();
  void reinitializeVSACLoader(String username, String password);

  CqlRule getCqlRule(String topic, String fhirVersion);

  FileResource getFile(String topic, String fileName, String fhirVersion, boolean convert);

  FileResource getFhirResourceByTopic(String fhirVersion, String resourceType, String name, String baseUrl);
  FileResource getFhirResourceById(String fhirVersion, String resourceType, String id, String baseUrl);

  // from RuleFinder
  List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria);
  List<RuleMapping> findAll();
}
