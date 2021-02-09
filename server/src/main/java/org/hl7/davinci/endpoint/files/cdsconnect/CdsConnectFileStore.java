package org.hl7.davinci.endpoint.files.cdsconnect;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.ShortNameMaps;
import org.hl7.davinci.SuppressParserErrorHandler;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.cql.CqlRule;
import org.hl7.davinci.endpoint.database.*;
import org.hl7.davinci.endpoint.files.*;
import org.hl7.davinci.endpoint.vsac.ValueSetCache;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

@Component
@Profile("cdsConnect")
public class CdsConnectFileStore extends CommonFileStore {

  static final Logger logger = LoggerFactory.getLogger(CdsConnectFileStore.class);

  @Autowired
  private CdsConnectConnection connection;

  public CdsConnectFileStore() {
    logger.info("Using CdsConnectFileStore");
  }

  public void reload() {
    long startTime = System.nanoTime();
    boolean success = true;

    // clear the database first
    lookupTable.deleteAll();
    fhirResources.deleteAll();

    logger.info("CdsConnectFileStore::reload()");

    // query for all of the Artifact Node IDs
    CdsConnectArtifactList artifactList = connection.queryForArtifactList();
    List<CdsConnectArtifact> artifacts = artifactList.getArtifacts();

    // add all of the files from the artifacts found to a single list
    for (CdsConnectArtifact artifact : artifacts) {

      String topic = artifact.getCode();

      ObjectMapper objectMapper = new ObjectMapper();

      String topicMetadataString = artifact.getTopicMetadata();

      // skip topics that are missing the TopicMetadata
      if (topicMetadataString.isEmpty()) {
        if (topic.equalsIgnoreCase(FileStore.SHARED_TOPIC)) {
          logger.error("  CdsConnectFileStore::reload(): Shared topic missing topic metadata");
          break;
        } else {
          logger.info("  CdsConnectFileStore::reload(): skipping topic: " + topic + " with missing topic metadata");
          continue;
        }
      }

      List<CdsConnectFile> files = artifact.getFiles();

      // build the rules table
      try {
        // convert to object
        TopicMetadata metadata = objectMapper.readValue(topicMetadataString, TopicMetadata.class);

        if (topic.equalsIgnoreCase(FileStore.SHARED_TOPIC)) {
          logger.info("  CdsConnectFileStore::reload() found Shared files");

          String mainCqlLibraryName = FileStore.FHIR_HELPERS_FILENAME;
          String mainCqlFile = findFile(files, mainCqlLibraryName, FileStore.CQL_EXTENSION);
          String mainCqlFilename = FilenameUtils.getName(mainCqlFile);

          for (String fhirVersion : metadata.getFhirVersions()) {

            logger.info("    Added: " + metadata.getTopic() + ": (" + fhirVersion + ")");

            // create table entry and store it back to the table
            RuleMapping ruleMappingEntry = new RuleMapping();
            ruleMappingEntry.setPayer("")
                .setCodeSystem("")
                .setCode("")
                .setFhirVersion(fhirVersion)
                .setTopic(metadata.getTopic())
                .setRuleFile(mainCqlFilename)
                .setRuleFilePath(mainCqlFile)
                .setNode(artifact.getId());
            lookupTable.save(ruleMappingEntry);
          }

        } else {
          logger.info("  CdsConnectFileStore::reload() found topic: " + topic);

          String mainCqlLibraryName = metadata.getTopic() + "Rule";
          String mainCqlFile = findFile(files, mainCqlLibraryName, FileStore.CQL_EXTENSION);
          String mainCqlFilename = FilenameUtils.getName(mainCqlFile);

          if (mainCqlFile == null) {
            logger.warn("CdsConnectFileStore::reloadFromFolder(): failed to find main CQL file for topic: "
                + metadata.getTopic());
          } else {

            for (String fhirVersion : metadata.getFhirVersions()) {
              for (Mapping mapping : metadata.getMappings()) {
                for (String code : mapping.getCodes()) {
                  for (String payer : metadata.getPayers()) {

                    logger.info("    Added: " + metadata.getTopic() + ": " + payer + ", "
                        + mapping.getCodeSystem() + ", " + code + " (" + fhirVersion + ")");

                    // create table entry and store it back to the table
                    RuleMapping ruleMappingEntry = new RuleMapping();
                    ruleMappingEntry.setPayer(ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.get(payer))
                        .setCodeSystem(
                            ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.get(mapping.getCodeSystem()))
                        .setCode(code)
                        .setFhirVersion(fhirVersion)
                        .setTopic(metadata.getTopic())
                        .setRuleFile(mainCqlFilename)
                        .setRuleFilePath(mainCqlFile)
                        .setNode(artifact.getId());
                    lookupTable.save(ruleMappingEntry);
                  }
                }
              }
            }
          }
        }
      } catch (IOException e) {
        logger.info("failed to process topic metadata");
        continue;
      }

      processFhirFiles(files, topic);
    }

    /*
    //uncomment to print contents of FhirResource table on reload
    // loop through the fhir resources table and print it out
    logger.info("FhirResource: " + FhirResource.getColumnsString());
    for (FhirResource resource : fhirResources.findAll()) {
      logger.info(resource.toString());
    }
    */

    long endTime = System.nanoTime();
    long timeElapsed = endTime - startTime;
    float seconds = (float) timeElapsed / (float) 1000000000;

    if (success) {
      logger.info("CdsConnectFileStore::reload(): completed in " + seconds + " seconds");
    } else {
      logger.warn("CdsConnectFileStore::reload(): failed in " + seconds + " seconds");
    }
  }

  private void processFhirFiles(List<CdsConnectFile> files, String topic) {
    // process the fhir resource files
    // setup the proper FHIR Context for the version of FHIR we are dealing with
    FhirContext r4ctx = new org.hl7.davinci.r4.FhirComponents().getFhirContext();
    IParser r4parser = r4ctx.newJsonParser();
    r4parser.setParserErrorHandler(new SuppressParserErrorHandler()); // suppress the unknown element warnings

    // process all of the files found within the topic/artifact
    for (CdsConnectFile file : files) {
      String path = file.getPath();
      String filename = file.getFilename();

      if (filename.endsWith(".json")) {
        logger.info("        process: FHIR Resource: " + filename);

        String[] parts = filename.split("-");
        if (parts.length > 2) {

          //String resourceType = parts[0];
          String fhirVersion = parts[1];
          String name = parts[2];

          IBaseResource baseResource = null;
          byte[] fileContents = file.getCqlBundle();
          if (fhirVersion.equalsIgnoreCase("R4")) {
            baseResource = r4parser.parseResource(new ByteArrayInputStream(fileContents));
          }

          processFhirResource(baseResource, path, filename, fhirVersion, topic);
        }
      }
    }
  }

  public CqlRule getCqlRule(String topic, String fhirVersion) {
    logger.info("CdsConnectFileStore::getCqlRule(): " + topic + "/" + fhirVersion);

    // load CQL files needed for the CRD Rule
    HashMap<String, byte[]> cqlFiles = new HashMap<>();

    String mainCqlLibraryName = topic + "Rule";

    // get the node for the matching topic from the database
    List<RuleMapping> rules = ruleFinder.findRules(topic, fhirVersion);
    if (rules.isEmpty()) {
      logger.info("CdsConnectFileStore::getCqlRule(): matching rule cannot be found");
      return new CqlRule();
    }
    RuleMapping rule = rules.get(0);

    // find the main CQL file
    CdsConnectFile file = new CdsConnectFile(connection, rule.getRuleFilePath());
    cqlFiles.put(file.getFilename(), file.getCqlBundle());
    logger.info("CdsConnectFileStore::getCqlRule(): added mainCqlFile: " + file.getFilename());

    // find the FHIRHelpers CQL file
    // get the node for the shared topic from the database
    List<RuleMapping> sharedRules = ruleFinder.findRules(FileStore.SHARED_TOPIC, fhirVersion);
    if (sharedRules.isEmpty()) {
      logger.info("CdsConnectFileStore::getCqlRule(): Shared info could not be found");
      return new CqlRule();
    }
    RuleMapping sharedRule = sharedRules.get(0);

    // get the matching artifact for the node
    // find the CQL helper file
    CdsConnectFile sharedFile = new CdsConnectFile(connection, sharedRule.getRuleFilePath());
    cqlFiles.put(sharedFile.getFilename(), sharedFile.getCqlBundle());
    logger.info("CdsConnectFileStore::getCqlRule(): added FHIRHelpers: " + sharedFile.getFilename());

    return new CqlRule(mainCqlLibraryName, cqlFiles, fhirVersion);
  }

  public FileResource getFile(String topic, String fileName, String fhirVersion, boolean convert) {
    FileResource fileResource = new FileResource();
    fileResource.setFilename(fileName);

    //TODO: maybe build a files table for quick lookup and retrieval
    // this would save a round trip to the server to get the topic/artifact

    // get the node for the matching topic from the database
    List<RuleMapping> rules = ruleFinder.findRules(topic, fhirVersion);
    if (rules.isEmpty()) {
      logger.info("CdsConnectFileStore::getFile(): matching rule cannot be found for topic: " + topic);
      return null;
    }
    RuleMapping rule = rules.get(0);

    // get the matching artifact for the node
    CdsConnectArtifact artifact = new CdsConnectArtifact(connection, connection.retrieveArtifact(rule.getNode()));
    List<CdsConnectFile> files = artifact.getFiles();

    // regex pattern will ignore the versioning that CDS Connect may add to the filename
    String extension = FilenameUtils.getExtension(fileName);
    String baseName = FilenameUtils.getBaseName(fileName);
    String regex = baseName + "(_\\d*)*." + extension;
    Pattern pattern = Pattern.compile(regex);

    Optional<String> foundFile = files.stream()
        .map(s-> s.getPath())
        .filter(pattern.asPredicate())
        .findFirst();

    if (!foundFile.isPresent()) {
      logger.info("CdsConnectFileStore: getFile(): matching file could not be found");
      return null;
    } else {

      // read the file
      CdsConnectFile file = new CdsConnectFile(connection, foundFile.get());
      byte[] fileData = file.getCqlBundle();

      // convert to ELM
      if (convert && FilenameUtils.getExtension(fileName).toUpperCase().equals("CQL")) {
        logger.info("CdsConnectFileStore::getFile() converting CQL to JSON ELM");

        // convert byte array to string
        String cql = new String(fileData);
        byte[] elmFileData = null;
        try {
          String elm = CqlExecution.translateToElm(cql, new CDSLibrarySourceProvider(this));
          elmFileData = elm.getBytes();
        } catch (Exception e) {
          logger.warn("CdsConnectFileStore::getFile() Error: could not convert CQL: " + e.getMessage());
          return null;
        }
        fileResource.setResource(new ByteArrayResource(elmFileData));

      } else {
        fileResource.setResource(new ByteArrayResource(fileData));
      }
    }

    return fileResource;
  }

  protected String readFhirResourceFromFile(FhirResource fhirResource, String fhirVersion) {
    String fileString = null;
    String filePath;
    InputStream inputStream;

    // If the topic indicates it's actually from the ValueSet cache. Grab file input stream from there.
    if (fhirResource.getTopic().equals(ValueSetCache.VSAC_TOPIC)) {
      filePath = config.getValueSetCachePath() + fhirResource.getFilename();
      try {
        inputStream = new FileInputStream(filePath);

        try {
          fileString = IOUtils.toString(inputStream, Charset.defaultCharset());
        } catch (IOException e) {
          logger.warn("CdsConnectFileStore::getFhirResourceByTopic() failed to get file: " + e.getMessage());
          return null;
        }
      } catch (FileNotFoundException e) {
        logger.warn("CdsConnectFileStore::readFhirResourceFromFile() Could not find ValueSet in cache folder.");
        return null;
      }
    } else {
      CdsConnectFile file = new CdsConnectFile(connection, fhirResource.getPath());
      byte[] fileData = file.getCqlBundle();
      fileString = new String(fileData);
    }

    return fileString;
  }

  protected String findFile(List<CdsConnectFile> files, String name, String extension) {
    // regex pattern will ignore the versioning that CDS Connect may add to the filename
    String regex = name + "-\\d.\\d.\\d(_\\d*)*" + extension;
    Pattern pattern = Pattern.compile(regex);

    Optional<String> match = files.stream()
        .map(s-> s.getPath())
        .filter(pattern.asPredicate())
        .findFirst();

    if (match.isPresent()) {
      return match.get();
    }

    logger.info("CdsConnectFileStore::findFile(): no files match: " + regex);
    return null;
  }
}
