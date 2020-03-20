package org.hl7.davinci.endpoint.files.cdsconnect;

import org.hl7.davinci.endpoint.cql.CqlRule;
import org.hl7.davinci.endpoint.database.RuleMapping;
import org.hl7.davinci.endpoint.files.FileResource;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
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

  public void reload() {
    long startTime = System.nanoTime();

    logger.info("CdsConnectFileStore::reload()");

    long endTime = System.nanoTime();
    long timeElapsed = endTime - startTime;
    float seconds = (float)timeElapsed / (float)1000000000;

    logger.info("CdsConnectFileStore::reload(): completed in " + seconds + " seconds");
  }

  public CqlRule getCqlRule(String topic, String fhirVersion) {
    logger.info("CdsConnectFileStore::getCqlRule(): " + topic + "/" + fhirVersion);
    return new CqlRule();
  }

  public FileResource getFile(String topic, String fileName, String fhirVersion, boolean convert) {
    FileResource fileResource = new FileResource();
    fileResource.setFilename(fileName);
    byte[] fileData = null;
    fileResource.setResource(new ByteArrayResource(fileData));
    return fileResource;
  }

  public FileResource getFhirResourceByTopic(String fhirVersion, String resourceType, String name, String baseUrl) {
    logger.info("CdsConnectFileStore::getFhirResourceByTopic(): " + fhirVersion + "/" + resourceType + "/" + name);
    // Library-R4-HomeOxygenTherapy-prepopulation.json
    //String fileName = resourceType + "-" + fhirVersion + "-"
    String filename = "";
    FileResource fileResource = new FileResource();
    fileResource.setFilename(filename);
    byte[] fileData = null;
    fileResource.setResource(new ByteArrayResource(fileData));
    return fileResource;
  }
  public FileResource getFhirResourceById(String fhirVersion, String resourceType, String id, String baseUrl) {
    logger.info("CdsConnectFileStore::getFhirResourceById(): " + fhirVersion + "/" + resourceType + "/" + id);
    String filename = "";
    FileResource fileResource = new FileResource();
    fileResource.setFilename(filename);
    byte[] fileData = null;
    fileResource.setResource(new ByteArrayResource(fileData));
    return fileResource;
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