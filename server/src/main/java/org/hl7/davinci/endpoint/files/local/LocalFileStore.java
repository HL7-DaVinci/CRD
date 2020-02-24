package org.hl7.davinci.endpoint.files.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.hl7.ShortNameMaps;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.cql.bundle.CqlRule;
import org.hl7.davinci.endpoint.database.RuleMapping;
import org.hl7.davinci.endpoint.database.RuleMappingRepository;
import org.hl7.davinci.endpoint.files.*;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

@Component
@Profile("localDb")
public class LocalFileStore implements FileStore {

  static final Logger logger = LoggerFactory.getLogger(LocalFileStore.class);

  @Autowired
  RuleFinder ruleFinder;

  @Autowired
  private RuleMappingRepository lookupTable;

  @Autowired
  YamlConfig config;

  @Autowired
  public LocalFileStore() {
    logger.info("Using LocalFileStore");
  }

  public void reload() {
    // clear the database first
    lookupTable.deleteAll();

    String path = config.getLocalDb().getPath();
    logger.info("LocalFileStore::reload(): " + path);
    File[] topics = new File(path).listFiles();
    for (File topic: topics) {
      if (topic.isDirectory()) {

        // skip the shared folder for now...
        if (topic.getName().equalsIgnoreCase("Shared")) {
          logger.info("  LocalFileStore::reload() found Shared files");
        } else {
          logger.info("  LocalFileStore::reload() found topic: " + topic.getName());

          // process the metadata file
          File[] files = topic.listFiles();
          for (File file: files) {
            if (file.getName().equalsIgnoreCase("TopicMetadata.json")) {
              ObjectMapper objectMapper = new ObjectMapper();

              try {
                // read the file
                String content = new String(Files.readAllBytes(file.toPath()));

                // convert to object
                TopicMetadata metadata = objectMapper.readValue(content, TopicMetadata.class);

                for (Mapping mapping: metadata.getMappings()) {
                  for (String code: mapping.getCodes()) {
                    for (String payer: metadata.getPayers()) {
                      for (String fhirVersion: metadata.getFhirVersions()) {

                        String mainCqlLibraryName = metadata.getTopic() + "Rule";
                        File mainCqlFile = findFile(metadata.getTopic(), fhirVersion, mainCqlLibraryName, ".cql");
                        if (mainCqlFile == null) {
                          logger.warn("LocalFileStore::reload(): failed to find main CQL file for topic: " + metadata.getTopic());
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
                              .setRuleFile(mainCqlFile.getName());
                          lookupTable.save(ruleMappingEntry);
                        }
                      }
                    }
                  }
                }

              } catch (IOException e) {
                logger.info("failed to open file: " + file.getAbsoluteFile());
              }
            }
          }
        }
      }
    }
  }

  public CqlRule getCqlRule(String topic, String fhirVersion) {
    logger.info("LocalFileStore::getCqlRule(): " + topic + "/" + fhirVersion);

    // load CQL files needed for the CRD Rule
    HashMap<String, byte[]> cqlFiles = new HashMap<>();

    String mainCqlLibraryName = topic + "Rule";
    File mainCqlFile = findFile(topic, fhirVersion, mainCqlLibraryName, ".cql");
    if (mainCqlFile == null) {
      logger.warn("LocalFileStore::getCqlRule(): failed to find main CQL file");
    } else {
      try {
        cqlFiles.put(mainCqlFile.getName(), Files.readAllBytes(mainCqlFile.toPath()));
        logger.info("LocalFileStore::getCqlRule(): added mainCqlFile: " + mainCqlFile.toPath());
      } catch (IOException e) {
        logger.warn("LocalFileStore::getCqlRule(): failed to open main cql file: " + e.getMessage());
      }
    }

    File helperCqlFile = findFile("Shared", fhirVersion, "FHIRHelpers", ".cql");
    if (mainCqlFile == null) {
      logger.warn("LocalFileStore::getCqlRule(): failed to find FHIR helper CQL file");
    } else {
      try {
        cqlFiles.put(helperCqlFile.getName(), Files.readAllBytes(helperCqlFile.toPath()));
        logger.info("LocalFileStore::getCqlRule(): added helperCqlFile: " + helperCqlFile.toPath());
      } catch (IOException e) {
        logger.warn("LocalFileStore::getCqlRule(): failed to open file FHIR helper cql file: " + e.getMessage());
      }
    }

    return new CqlRule(mainCqlLibraryName, cqlFiles, fhirVersion);
  }

  public FileResource getFile(String topic, String fileName, String fhirVersion, boolean convert) {
    FileResource fileResource = new FileResource();
    fileResource.setFilename(fileName);

    String localPath = config.getLocalDb().getPath();
    String filePath = localPath + topic + "/" + fhirVersion + "/files/" + fileName;
    File file = new File(filePath);
    byte[] fileData = null;
    try {
      fileData = Files.readAllBytes(file.toPath());

      // convert to ELM
      if (convert && FilenameUtils.getExtension(fileName).toUpperCase().equals("CQL")) {
        logger.info("LocalFileStore::getFile() converting CQL to JSON ELM");
        String cql = new String(fileData);
        try {
          String elm = CqlExecution.translateToElm(cql);
          fileData = elm.getBytes();
        } catch (Exception e) {
          logger.warn("LocalFileStore::getFile() Error: could not convert CQL: " + e.getMessage());
          return null;
        }
      }
    } catch (IOException e) {
      logger.warn("LocalFileStore::getFile() failed to get file: " + e.getMessage());
      return null;
    }

    fileResource.setResource(new ByteArrayResource(fileData));
    return fileResource;
  }

  public List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria) {
    logger.info("LocalFileStore::findRules(): " + criteria.toString());
    return ruleFinder.findRules(criteria);
  }

  public List<RuleMapping> findAll() {
    logger.info("LocalFileStore::findAll()");
    return ruleFinder.findAll();
  }

  private File findFile(String topic, String fhirVersion, String name, String extension) {
    String localPath = config.getLocalDb().getPath();
    String cqlFileLocation = localPath + topic + "/" + fhirVersion + "/files/";
    File dir = new File(cqlFileLocation);
    String regex = name + "-\\d.\\d.\\d" + extension;
    FileFilter fileFilter = new RegexFileFilter(regex);
    File[] files = dir.listFiles(fileFilter);
    for (int i=0; i < files.length; i++) {
      //logger.info("LocalFileStore::findFile(): matched file: " + files[i]);
      return files[i];
    }
    logger.info("LocalFileStore::findFile(): no files match: " + cqlFileLocation + regex);
    return null;
  }
}
