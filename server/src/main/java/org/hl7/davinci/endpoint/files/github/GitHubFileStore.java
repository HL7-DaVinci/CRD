package org.hl7.davinci.endpoint.files.github;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.io.InputStream;

import org.zeroturnaround.zip.ZipUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Component
@Profile("gitHub")
public class GitHubFileStore extends CommonFileStore {

  static final Logger logger = LoggerFactory.getLogger(GitHubFileStore.class);

  @Autowired
  GitHubConnection connection;

  public GitHubFileStore() {
    logger.info("Using GitHubFileStore");
  }

  public void reload() {

    long startTime = System.nanoTime();
    boolean success = true;

    // clear the database first
    lookupTable.deleteAll();
    fhirResources.deleteAll();

    if (config.getGitHubConfig().getUseZipForReload()) {
      success = reloadFromZip();

    } else {
      String rulePath = config.getGitHubConfig().getRulePath();
      success = reloadFromGitHub(rulePath);

      // Load the examples folder
      if (success) {
        String examplesPath = config.getGitHubConfig().getExamplesPath();
        success = reloadFromGitHub(examplesPath);
      }
    }

    long endTime = System.nanoTime();
    long timeElapsed = endTime - startTime;
    float seconds = (float) timeElapsed / (float) 1000000000;

    if (success) {
      logger.info("GitHubFileStore::reload(): completed in " + seconds + " seconds");
    } else {
      logger.warn("GitHubFileStore::reload(): failed in " + seconds + " seconds");
    }
  }

  private boolean reloadFromZip() {
    logger.info("GitHubFileStore::reloadFromZip()");
    // download the repo
    String zipPath = connection.downloadRepo();
    File zipFile = new File(zipPath);

    // unzip the file in place (folder will be name of zip file)
    ZipUtil.explode(zipFile);

    // get a list of files in the directory that was unzipped
    File[] files = zipFile.listFiles();
    File location = null;
    if (files.length > 0) {
      if (files[0].isDirectory()) {
        location = files[0];
      }
    }
    if (location != null) {

      // load the folder
      String rulePath = config.getGitHubConfig().getRulePath();
      String path = location.getPath() + "/" + rulePath;
      try {
        reloadFromFolder(path + "/");
      } catch (IOException e) {
        logger.error("FATAL ERROR: Failed to reload from folder: " + e.getMessage());
        System.exit(1);
      }

      // load the examples folder
      String examplesPath = config.getGitHubConfig().getExamplesPath();
      path = location.getPath() + "/" + examplesPath;
      try {
        reloadFromFolder(path + "/");
      } catch (IOException e) {
        logger.error("FATAL ERROR: Failed to reload from examples folder: " + e.getMessage());
        System.exit(1);
      }

      // clean up the zip file
      try {
        FileUtils.deleteDirectory(zipFile);
      } catch (IOException e) {
        logger.warn("GitHubFileStore::reloadFromZip() failed to delete directory: " + e.getMessage());
        return false;
      }
    } else {
      return false;
    }
    return true;
  }

  private boolean reloadFromGitHub(String rulePath) {
    logger.info("GitHubFileStore::reloadFromGitHub(): " + rulePath);

    for (String topicName : connection.getDirectory(rulePath)) {
      String topicPath = rulePath + topicName;
      // skip files with an extension or folders that start with a '.'
      if (!topicName.contains(".")) {

        // skip the shared folder for now...
        if (topicName.equalsIgnoreCase(FileStore.SHARED_TOPIC)) {
          logger.info("  GitHubFileStore::reloadFromGitHub() found Shared files");

          for (String fhirFolder : connection.getDirectory(topicPath)) {
            String fhirVersion = fhirFolder;
            String fullPath = topicPath + "/" + fhirFolder;
            processFhirFolder(topicName, fhirVersion, fullPath);
          }

        } else if (topicName.startsWith(".")) {
          // logger.info(" GitHubFileStore::reloadFromGitHub() skipping all folders starting with .: " + topicName);
        } else {
          logger.info("  GitHubFileStore::reloadFromGitHub() found topic: " + topicName);

          // process the metadata file
          for (String fileName : connection.getDirectory(topicPath)) {

            if (fileName.equalsIgnoreCase("TopicMetadata.json")) {
              ObjectMapper objectMapper = new ObjectMapper();

              String fullPath = rulePath + topicName + "/" + fileName;
              try {
                // read the file
                InputStream inputStream = connection.getFile(fullPath);
                String content = IOUtils.toString(inputStream, Charset.defaultCharset());

                // convert to object
                TopicMetadata metadata = objectMapper.readValue(content, TopicMetadata.class);

                for (Mapping mapping : metadata.getMappings()) {
                  for (String code : mapping.getCodes()) {
                    for (String payer : metadata.getPayers()) {
                      for (String fhirVersion : metadata.getFhirVersions()) {

                        String mainCqlLibraryName = metadata.getTopic() + "Rule";
                        String mainCqlFile = findGitHubFile(metadata.getTopic(), fhirVersion, mainCqlLibraryName, FileStore.CQL_EXTENSION);
                        if (mainCqlFile == null) {
                          logger.warn("GitHubFileStore::reloadFromGitHub(): failed to find main CQL file for topic: " + metadata.getTopic());
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
            } else {
              String fhirVersion = fileName;
              String fullPath = topicPath + "/" + fileName;
              processFhirFolder(topicName, fhirVersion, fullPath);
            }
          }
        }
      }
    }
    return true;
  }

  private void processFhirFolder(String topic, String fhirVersion, String fhirPath) {
    fhirVersion = fhirVersion.toUpperCase();
    logger.info("      GitHubFileStore::processFhirFolder(): " + fhirVersion + ": " + fhirPath);

    // setup the proper FHIR Context for the version of FHIR we are dealing with
    FhirContext ctx = null;
    if (fhirVersion.equalsIgnoreCase("R4")) {
      ctx = new org.hl7.davinci.r4.FhirComponents().getFhirContext();
    } else {
      logger.warn("unsupported FHIR version: " + fhirVersion + ", skipping folder");
      return;
    }
    IParser parser = ctx.newJsonParser();
    parser.setParserErrorHandler(new SuppressParserErrorHandler()); // suppress the unknown element warnings

    for (String folder : connection.getDirectory(fhirPath)) {
      if (folder.equalsIgnoreCase("resources")) {

        String fullFolderPath = fhirPath + "/" + folder;

        for (String resource : connection.getDirectory(fullFolderPath)) {
          String filename = resource;
          String fullFilePath = fullFolderPath + "/" + filename;
          logger.info("        process: FHIR Resource: " + filename);

          String[] parts = filename.split("-");
          if (parts.length > 2) {
            String resourceType;// = parts[0];

            if (!parts[1].equalsIgnoreCase(fhirVersion)) {
              logger.warn("GitHubFileStore::processFhirFolder() warning: FhirVersion doesn't match!");
              continue;
            }

            InputStream inputStream = connection.getFile(fullFilePath);
            if (inputStream != null) {
              IBaseResource baseResource = parser.parseResource(inputStream);

              processFhirResource(baseResource, filename, filename, fhirVersion, topic);

            } else {
              logger.warn("could not find file: " + fullFilePath);
              continue;
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

    String rulePath = config.getGitHubConfig().getRulePath();
    String examplesPath = config.getGitHubConfig().getExamplesPath();

    String mainCqlLibraryName = topic + "Rule";
    String mainCqlFile = findGitHubFile(topic, fhirVersion, mainCqlLibraryName, FileStore.CQL_EXTENSION);
    if (mainCqlFile == null) {
      logger.warn("GitHubFileStore::getCqlRule(): failed to find main CQL file");
    } else {
      String mainCqlFilePath = rulePath + topic + "/" + fhirVersion + "/files/" + mainCqlFile;
      try {
        InputStream inputStream = connection.getFile(mainCqlFilePath);
        if (inputStream == null) {
          // look for the main cql file in the examples path as well
          mainCqlFilePath = examplesPath + topic + "/" + fhirVersion + "/files/" + mainCqlFile;
          inputStream = connection.getFile(mainCqlFilePath);
        }
        cqlFiles.put(mainCqlFile, IOUtils.toByteArray(inputStream));
        logger.info("GitHubFileStore::getCqlRule(): added mainCqlFile: " + mainCqlFile);
      } catch (IOException e) {
        logger.warn("GitHubFileStore::getCqlRule(): failed to open main cql file: " + e.getMessage());
      }
    }

    String helperCqlFile = findGitHubFile(FileStore.SHARED_TOPIC, fhirVersion, FileStore.FHIR_HELPERS_FILENAME, FileStore.CQL_EXTENSION);
    if (helperCqlFile == null) {
      logger.warn("GitHubFileStore::getCqlRule(): failed to find FHIR helper CQL file");
    } else {
      String helperCqlFilePath = rulePath + "Shared/" + fhirVersion + "/files/" + helperCqlFile;
      try {
        InputStream inputStream = connection.getFile(helperCqlFilePath);
        if (inputStream == null) {
          // look for the helper cql file in the examples path as well
          helperCqlFilePath = examplesPath + "Shared/" + fhirVersion + "/files/" + helperCqlFile;
          inputStream = connection.getFile(helperCqlFilePath);
        }
        cqlFiles.put(helperCqlFile, IOUtils.toByteArray(inputStream));
        logger.info("GitHubFileStore::getCqlRule(): added helperCqlFile: " + helperCqlFile);
      } catch (IOException e) {
        logger.warn("GitHubFileStore::getCqlRule(): failed to open file FHIR helper cql file: " + e.getMessage());
      }
    }

    return new CqlRule(mainCqlLibraryName, cqlFiles, fhirVersion);
  }

  public FileResource getFile(String topic, String fileName, String fhirVersion, boolean convert) {
    FileResource fileResource = new FileResource();
    fhirVersion = fhirVersion.toUpperCase();
    fileResource.setFilename(fileName);

    String rulePath = config.getGitHubConfig().getRulePath();
    String filePath = rulePath + topic + "/" + fhirVersion + "/files/" + fileName;
    InputStream inputStream = connection.getFile(filePath);

    if (inputStream == null) {

      // Look in the examples folder
      String examplesPath = config.getGitHubConfig().getExamplesPath();
      filePath = examplesPath + topic + "/" + fhirVersion + "/files/" + fileName;
      inputStream = connection.getFile(filePath);

      if (inputStream == null) {
        logger.warn("GitHubFileStore:getFile() Error getting file");
        return null;
      }
    }

    // convert to ELM
    if (convert && FilenameUtils.getExtension(fileName).toUpperCase().equals("CQL")) {
      logger.info("GitHubFileStore::getFile() converting CQL to JSON ELM");

      try {
        String cql = IOUtils.toString(inputStream, Charset.defaultCharset());
        byte[] fileData = null;
        try {
          String elm = CqlExecution.translateToElm(cql, new CDSLibrarySourceProvider(this));
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


  protected String readFhirResourceFromFile(FhirResource fhirResource, String fhirVersion) {
    String fileString = null;
    String filePath;
    InputStream inputStream;

    // If the topic indicates it's actually from the ValueSet cache. Grab file input stream from there.
    if (fhirResource.getTopic().equals(ValueSetCache.VSAC_TOPIC)) {
      filePath = config.getValueSetCachePath() + fhirResource.getFilename();
      try {
        inputStream = new FileInputStream(filePath);
      } catch (FileNotFoundException e) {
        logger.warn("GitHubFileStore::readFhirResourceFromFile() Could not find ValueSet in cache folder.");
        return null;
      }
    } else {
      String rulePath = config.getGitHubConfig().getRulePath();
      filePath = rulePath + fhirResource.getTopic() + "/" + fhirVersion + "/resources/" + fhirResource.getFilename();
      inputStream = connection.getFile(filePath);
    }

    if (inputStream == null) {
      // try to get the file from the examples folder
      String examplesPath = config.getGitHubConfig().getExamplesPath();
      filePath = examplesPath + fhirResource.getTopic() + "/" + fhirVersion + "/resources/" + fhirResource.getFilename();
      inputStream = connection.getFile(filePath);

      if (inputStream == null) {
        logger.warn("GitHubFileStore::readFhirResourceFromFile() Error getting file");
        return null;
      }
    }

    try {
      fileString = IOUtils.toString(inputStream, Charset.defaultCharset());
    } catch (IOException e) {
      logger.warn("GitHubFileStore::readFhirResourceFromFile() failed to get file: " + e.getMessage());
      return null;
    }

    return fileString;
  }

  private String findGitHubFileInPath(String rulePath, String topic, String fhirVersion, String name, String extension) {
    String cqlFileLocation = rulePath + topic + "/" + fhirVersion + "/files/";
    for (String file : connection.getDirectory(cqlFileLocation)) {
      if (file.startsWith(name) && file.endsWith(extension)) {
        return file;
      }
    }
    logger.info("GitHubFileStore::findGitHubFileInPath(): no files match: " + cqlFileLocation + name + "*.*.*" + extension);
    return null;
  }

  private String findGitHubFile(String topic, String fhirVersion, String name, String extension) {
    String rulePath = config.getGitHubConfig().getRulePath();
    String file = findGitHubFileInPath(rulePath, topic, fhirVersion, name, extension);

    // look in the examples path
    if (file == null) {
      String examplesPath = config.getGitHubConfig().getExamplesPath();
      file = findGitHubFileInPath(examplesPath, topic, fhirVersion, name, extension);
    }

    return file;
  }
}
