package org.hl7.davinci.endpoint.files.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.ShortNameMaps;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.cql.bundle.CqlRule;
import org.hl7.davinci.endpoint.database.RuleMapping;
import org.hl7.davinci.endpoint.database.RuleMappingRepository;
import org.hl7.davinci.endpoint.files.*;
import org.hl7.davinci.endpoint.github.GitHubConnection;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.InputStream;

@Component
@Profile("gitHub")
public class GitHubFileStore implements FileStore {

  static final Logger logger = LoggerFactory.getLogger(GitHubFileStore.class);

  @Autowired
  RuleFinder ruleFinder;

  @Autowired
  private RuleMappingRepository lookupTable;

  @Autowired
  GitHubConnection connection;

  @Autowired
  YamlConfig config;

  public GitHubFileStore() {
    logger.info("Using GitHubFileStore");
  }

  public void reload() {
    logger.info("GitHubFileStore::reload()");

    String rulePath = config.getGitHubConfig().getRulePath();
    for (String topicName : connection.getDirectory(rulePath)) {
      // skip files with an extension or folders that start with a '.'
      if (!topicName.contains(".")) {

        // skip the shared folder for now...
        if (topicName.equalsIgnoreCase("Shared")) {
          logger.info("  GitHubFileStore::reload() found Shared files");
        } else if (topicName.startsWith(".")) {
          //logger.info("  GitHubFileStore::reload() skipping all folders starting with .: " + topicName);
        } else {
          logger.info("  GitHubFileStore::reload() found topic: " + topicName);

          // process the metadata file
          for (String fileName : connection.getDirectory(topicName)) {


            if (fileName.equalsIgnoreCase("TopicMetadata.json")) {
              ObjectMapper objectMapper = new ObjectMapper();

              String fullPath = topicName + "/" + fileName;
              try {
                // read the file
                InputStream inputStream = connection.getFile(fullPath);
                String content = IOUtils.toString(inputStream, Charset.defaultCharset());

                // convert to object
                TopicMetadata metadata = objectMapper.readValue(content, TopicMetadata.class);

                for (Mapping mapping: metadata.getMappings()) {
                  for (String code: mapping.getCodes()) {
                    for (String payer: metadata.getPayers()) {
                      for (String fhirVersion: metadata.getFhirVersions()) {

                        String mainCqlLibraryName = metadata.getTopic() + "Rule";
                        String mainCqlFile = findFile(metadata.getTopic(), fhirVersion, mainCqlLibraryName, ".cql");
                        if (mainCqlFile == null) {
                          logger.warn("GitHubFileStore::reload(): failed to find main CQL file for topic: " + metadata.getTopic());
                        } else {
                          logger.info("    Added: " + metadata.getTopic() + ": " + payer + ", "
                              + mapping.getCodeSystem() + ", " + code + " (" + fhirVersion + ")");

                          // create table entry and store it back to the table
                          RuleMapping ruleMappingEntry = new RuleMapping();
                          ruleMappingEntry.setPayer(ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.get(payer))
                              .setCodeSystem(ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.get(mapping.getCodeSystem()))
                              .setCode(code)
                              .setFhirVersion(fhirVersion)
                              .setTopic(metadata.getTopic())
                              .setRuleFile(mainCqlFile);
                          lookupTable.save(ruleMappingEntry);
                        }
                      }
                    }
                  }
                }

              } catch (IOException e) {
                logger.info("failed to open file: " + fullPath);
              }
            }
          }
        }


      }
    }
  }

  public CqlRule getCqlRule(String topic, String fhirVersion) {
    logger.info("GitHubFileStore::getCqlRule(): " + topic + "/" + fhirVersion);

    // load CQL files needed for the CRD Rule
    HashMap<String, byte[]> cqlFiles = new HashMap<>();

    String mainCqlLibraryName = topic + "Rule";
    String mainCqlFile = findFile(topic, fhirVersion, mainCqlLibraryName, ".cql");
    if (mainCqlFile == null) {
      logger.warn("GitHubFileStore::getCqlRule(): failed to find main CQL file");
    } else {
      String mainCqlFilePath = topic + "/" + fhirVersion + "/files/" + mainCqlFile;
      try {
        cqlFiles.put(mainCqlFile, IOUtils.toByteArray(connection.getFile(mainCqlFilePath)));
        logger.info("GitHubFileStore::getCqlRule(): added mainCqlFile: " + mainCqlFile);
      } catch (IOException e) {
        logger.warn("GitHubFileStore::getCqlRule(): failed to open main cql file: " + e.getMessage());
      }
    }

    String helperCqlFile = findFile("Shared", fhirVersion, "FHIRHelpers", ".cql");
    if (helperCqlFile == null) {
      logger.warn("GitHubFileStore::getCqlRule(): failed to find FHIR helper CQL file");
    } else {
      String helperCqlFilePath = "Shared/" + fhirVersion + "/files/" + helperCqlFile;
      try {
        cqlFiles.put(helperCqlFile, IOUtils.toByteArray(connection.getFile(helperCqlFilePath)));
        logger.info("GitHubFileStore::getCqlRule(): added helperCqlFile: " + helperCqlFile);
      } catch (IOException e) {
        logger.warn("GitHubFileStore::getCqlRule(): failed to open file FHIR helper cql file: " + e.getMessage());
      }
    }

    return new CqlRule(mainCqlLibraryName, cqlFiles, fhirVersion);
  }

  public FileResource getFile(String topic, String fileName, String fhirVersion, boolean convert) {
    FileResource fileResource = new FileResource();
    fileResource.setFilename(fileName);

    String filePath = topic + "/" + fhirVersion + "/files/" + fileName;

    InputStream inputStream = connection.getFile(filePath);

    // convert to ELM
    if (convert && FilenameUtils.getExtension(fileName).toUpperCase().equals("CQL")) {
      logger.info("GitHubFileStore::getFile() converting CQL to JSON ELM");

      try {
        String cql = IOUtils.toString(inputStream, Charset.defaultCharset());
        byte[] fileData = null;
        try {
          String elm = CqlExecution.translateToElm(cql);
          fileData = elm.getBytes();
        } catch (Exception e) {
          logger.warn("GitHubFileStore::getFile() Error: could not convert CQL: " + e.getMessage());
          return null;
        }
        fileResource.setResource(new ByteArrayResource(fileData));
      } catch (IOException e) {
        logger.warn("GitHubFileStore::getFile() Error: could not read file: " + e.getMessage());
        return null;
      }
    } else {
      fileResource.setResource(new InputStreamResource(inputStream));
    }

    return fileResource;
  }

  public List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria) {
    logger.info("GitHubFileStore::findRules(): " + criteria.toString());
    return ruleFinder.findRules(criteria);
  }

  public List<RuleMapping> findAll() {
    logger.info("GitHubFileStore::findAll()");
    return ruleFinder.findAll();
  }

  private String findFile(String topic, String fhirVersion, String name, String extension) {
    String cqlFileLocation =  topic + "/" + fhirVersion + "/files/";
    for (String file : connection.getDirectory(cqlFileLocation)) {
      if (file.startsWith(name) && file.endsWith(extension)) {
        return file;
      }
    }
    logger.info("GitHubFileStore::findFile(): no files match: " + cqlFileLocation + name + "*.*.*" + extension);
    return null;
  }
}
