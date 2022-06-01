package org.hl7.davinci.endpoint.files;

import java.util.List;

import org.hl7.davinci.endpoint.cql.CqlRule;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
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

  // Get FHIR Resources as FileResource
  FileResource getFhirResourceByName(String fhirVersion, String resourceType, String name, String baseUrl);
  FileResource getFhirResourceById(String fhirVersion, String resourceType, String id, String baseUrl);
  FileResource getFhirResourceById(String fhirVersion, String resourceType, String id, String baseUrl, boolean isRoot);
  FileResource getFhirResourceByUrl(String fhirVersion, String resourceType, String url, String baseUrl);
  List<FileResource> getFhirResourcesByTopic(String fhirVersion, String resourceType, String topic, String baseUrl);
  FileResource getFhirResourcesByTopicAsBundle(String fhirVersion, String resourceType, String topic, String baseUrl);
  
  // Get FHIR Resources as FHIR Resources
  Resource getFhirResourceByIdAsFhirResource(String fhirVersion, String resourceType, String id, String baseUrl);
  Resource getFhirResourceByUrlAsFhirResource(String fhirVersion, String resourceType, String url, String baseUrl);
  Bundle getFhirResourcesByTopicAsFhirBundle(String fhirVersion, String resourceType, String topic, String baseUrl);

  // from RuleFinder
  List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria);
  List<RuleMapping> findAllRules();

  List<FhirResource> findAllFhirResources();
}
