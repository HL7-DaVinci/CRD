package org.hl7.davinci.endpoint.files.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.hl7.ShortNameMaps;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.cql.bundle.CqlRule;
import org.hl7.davinci.endpoint.database.RuleMapping;
import org.hl7.davinci.endpoint.database.RuleMappingRepository;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.files.Mapping;
import org.hl7.davinci.endpoint.files.RuleFinder;
import org.hl7.davinci.endpoint.files.TopicMetadata;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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

  public void update() {
    String path = config.getLocalDb().getPath();
    logger.info("LocalFileStore::update(): " + path);
    File[] topics = new File(path).listFiles();
    for (File topic: topics) {
      if (topic.isDirectory()) {

        // skip the shared folder for now...
        if (topic.getName().equalsIgnoreCase("Shared")) {
          logger.info("  LocalFileStore::update() found Shared files");
        } else {
          logger.info("  LocalFileStore::update() found topic: " + topic.getName());

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
                        logger.info("    Added: " + metadata.getTopic() + ": " + payer + ", "
                            + mapping.getCodeSystem() + ", " + code + " (" + fhirVersion + ")");

                        // create table entry and store it back to the table
                        RuleMapping ruleMappingEntry = new RuleMapping();
                        ruleMappingEntry.setPayer(ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.get(payer))
                            .setCodeSystem(ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.get(mapping.getCodeSystem()))
                            .setCode(code)
                            .setFhirVersion(fhirVersion)
                            .setTopic(metadata.getTopic());
                        lookupTable.save(ruleMappingEntry);
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
    File mainCqlFile = findCqlFile(topic, mainCqlLibraryName, fhirVersion);
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

    File helperCqlFile = findCqlFile("Shared", "FHIRHelpers", fhirVersion);
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

  private File findCqlFile(String topic, String name, String fhirVersion) {
    String localPath = config.getLocalDb().getPath();
    String cqlFileLocation = localPath + topic + "/" + fhirVersion + "/files/";
    File dir = new File(cqlFileLocation);
    String regex = name + "-\\d.\\d.\\d.cql";
    FileFilter fileFilter = new RegexFileFilter(regex);
    File[] files = dir.listFiles(fileFilter);
    for (int i=0; i < files.length; i++) {
      //logger.info("LocalFileStore::findCqlFile(): matched file: " + files[i]);
      return files[i];
    }
    logger.info("LocalFileStore::findCqlFile(): no files match: " + cqlFileLocation + regex);
    return null;
  }

  public List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria) {
    logger.info("LocalFileStore::findRules(): " + criteria.toString());
    return ruleFinder.findRules(criteria);
  }

  public List<RuleMapping> findAll() {
    logger.info("LocalFileStore::findAll()");
    return ruleFinder.findAll();
  }
}
