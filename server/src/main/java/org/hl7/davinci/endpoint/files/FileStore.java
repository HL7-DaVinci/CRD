package org.hl7.davinci.endpoint.files;

import java.util.List;

import org.hl7.davinci.endpoint.cql.CqlRule;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.database.RuleMapping;
import org.hl7.davinci.endpoint.database.FhirResource;

public interface FileStore {

  String SHARED_TOPIC = "Shared";
  String FHIR_HELPERS_FILENAME = "FHIRHelpers";
  String CQL_EXTENSION = ".cql";

  void reload();
  void reinitializeVSACLoader();
  void reinitializeVSACLoader(String apiKey);

  CqlRule getCqlRule(String topic, String fhirVersion);

  FileResource getFile(String topic, String fileName, String fhirVersion, boolean convert);

  FileResource getFhirResourceByTopic(String fhirVersion, String resourceType, String name, String baseUrl);
  FileResource getFhirResourceById(String fhirVersion, String resourceType, String id, String baseUrl);
  FileResource getFhirResourceById(String fhirVersion, String resourceType, String id, String baseUrl, boolean isRoot);
  FileResource getFhirResourceByUrl(String fhirVersion, String resourceType, String url, String baseUrl);

  // from RuleFinder
  List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria);
  List<RuleMapping> findAllRules();

  List<FhirResource> findAllFhirResources();
}
