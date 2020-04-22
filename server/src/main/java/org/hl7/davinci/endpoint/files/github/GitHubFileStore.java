package org.hl7.davinci.endpoint.files.github;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.ShortNameMaps;
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

    logger.info("GitHubFileStore::reload()");

    if (config.getGitHubConfig().getUseZipForReload()) {
      success = reloadFromZip();
    } else {
      success = reloadFromGitHub();
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
      String rulePath = config.getGitHubConfig().getRulePath();
      String path = location.getPath() + "/" + rulePath;

      // load the folder
      try {
        reloadFromFolder(path + "/");
      } catch (IOException e) {
        logger.error("FATAL ERROR: Failed to reload from folder: " + e.getMessage());
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

  private boolean reloadFromGitHub() {
    String rulePath = config.getGitHubConfig().getRulePath();

    for (String topicName : connection.getDirectory(rulePath)) {
      // skip files with an extension or folders that start with a '.'
      if (!topicName.contains(".")) {

        // skip the shared folder for now...
        if (topicName.equalsIgnoreCase("Shared")) {
          logger.info("  GitHubFileStore::reloadFromGitHub() found Shared files");

          for (String fhirFolder : connection.getDirectory(topicName)) {
            String fhirVersion = fhirFolder;
            String fullPath = topicName + "/" + fhirFolder;
            processFhirFolder(topicName, fhirVersion, fullPath);
          }

        } else if (topicName.startsWith(".")) {
          // logger.info(" GitHubFileStore::reloadFromGitHub() skipping all folders starting with .: " + topicName);
        } else {
          logger.info("  GitHubFileStore::reloadFromGitHub() found topic: " + topicName);

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

                for (Mapping mapping : metadata.getMappings()) {
                  for (String code : mapping.getCodes()) {
                    for (String payer : metadata.getPayers()) {
                      for (String fhirVersion : metadata.getFhirVersions()) {

                        String mainCqlLibraryName = metadata.getTopic() + "Rule";
                        String mainCqlFile = findGitHubFile(metadata.getTopic(), fhirVersion, mainCqlLibraryName, ".cql");
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
              String fullPath = topicName + "/" + fileName;
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
    } else if (fhirVersion.equalsIgnoreCase("STU3")) {
      ctx = new org.hl7.davinci.stu3.FhirComponents().getFhirContext();
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

            // parse the the resource file into the correct FHIR resource
            String resourceId = "";
            String resourceName = "";
            InputStream inputStream = connection.getFile(fullFilePath);
            if (inputStream != null) {
              IBaseResource baseResource = parser.parseResource(inputStream);
              resourceType = baseResource.fhirType(); // grab the FHIR resource type out of the resource
              resourceType = resourceType.toLowerCase();

              if (fhirVersion.equalsIgnoreCase("R4")) {
                if (resourceType.equalsIgnoreCase("Questionnaire")) {
                  org.hl7.fhir.r4.model.Questionnaire questionnaire = (org.hl7.fhir.r4.model.Questionnaire) baseResource;
                  resourceId = questionnaire.getId();
                  resourceName = questionnaire.getName();
                } else if (resourceType.equalsIgnoreCase("Library")) {
                  org.hl7.fhir.r4.model.Library library = (org.hl7.fhir.r4.model.Library) baseResource;
                  resourceId = library.getId();
                  resourceName = library.getName();
                } else if (resourceType.equalsIgnoreCase("ValueSet")) {
                  org.hl7.fhir.r4.model.ValueSet valueSet = (org.hl7.fhir.r4.model.ValueSet) baseResource;
                  resourceId = valueSet.getId();
                  resourceName = valueSet.getName();
                }
              } else if (fhirVersion.equalsIgnoreCase("STU3")) {
                if (resourceType.equalsIgnoreCase("Questionnaire")) {
                  org.hl7.fhir.dstu3.model.Questionnaire questionnaire = (org.hl7.fhir.dstu3.model.Questionnaire) baseResource;
                  resourceId = questionnaire.getId();
                  resourceName = questionnaire.getName();
                } else if (resourceType.equalsIgnoreCase("Library")) {
                  org.hl7.fhir.dstu3.model.Library library = (org.hl7.fhir.dstu3.model.Library) baseResource;
                  resourceId = library.getId();
                  resourceName = library.getName();
                } else if (resourceType.equalsIgnoreCase("ValueSet")) {
                  org.hl7.fhir.dstu3.model.ValueSet valueSet = (org.hl7.fhir.dstu3.model.ValueSet) baseResource;
                  resourceId = valueSet.getId();
                  resourceName = valueSet.getName();
                }
              }

            } else {
              logger.warn("could not find file: " + fullFilePath);
              continue;
            }

            if (resourceId == null) {
              // this should never happen, there should always be an ID
              logger.error("Could not find ID for: " + filename + ", defaulting to '" + filename + "' as the ID");
              resourceId = filename;
            }

            if (resourceName == null) {
              resourceName = stripNameFromResourceFilename(filename, fhirVersion);
              logger.info("Could not find name for: " + filename + ", defaulting to '" + resourceName + "' as the name");
            }

            resourceId = resourceId.toLowerCase();
            resourceName = resourceName.toLowerCase();

            // create a FhirResource and save it back to the table
            FhirResource fhirResource = new FhirResource();
            fhirResource.setId(resourceId)
                .setFhirVersion(fhirVersion)
                .setResourceType(resourceType)
                .setTopic(topic)
                .setFilename(filename)
                .setName(resourceName);
            fhirResources.save(fhirResource);
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
    String mainCqlFile = findGitHubFile(topic, fhirVersion, mainCqlLibraryName, ".cql");
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

    String helperCqlFile = findGitHubFile("Shared", fhirVersion, "FHIRHelpers", ".cql");
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

    if (inputStream == null) {
      logger.warn("GitHubFileStore:getFile() Error getting file");
      return null;
    }

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


  protected FileResource readFhirResourceFromFile(List<FhirResource> fhirResourceList, String fhirVersion, String baseUrl) {
    byte[] fileData = null;

    if (fhirResourceList.size() > 0) {
      // just return the first matched resource
      FhirResource fhirResource = fhirResourceList.get(0);

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
        filePath = fhirResource.getTopic() + "/" + fhirVersion + "/resources/" + fhirResource.getFilename();
        inputStream = connection.getFile(filePath);
      }

      if (inputStream == null) {
        logger.warn("GitHubFileStore::readFhirResourceFromFile() Error getting file");
        return null;
      }

      try {
        // replace <server-path> with the proper path
        String partialUrl = baseUrl + "fhir/" + fhirVersion + "/";

        String fileString = IOUtils.toString(inputStream, Charset.defaultCharset());
        fileString = fileString.replace("<server-path>", partialUrl);
        fileData = fileString.getBytes(Charset.defaultCharset());

        FileResource fileResource = new FileResource();
        fileResource.setFilename(fhirResource.getFilename());
        fileResource.setResource(new ByteArrayResource(fileData));
        return fileResource;

      } catch (IOException e) {
        logger.warn("GitHubFileStore::getFhirResourceByTopic() failed to get file: " + e.getMessage());
        return null;
      }

    } else {
      return null;
    }
  }

  private String findGitHubFile(String topic, String fhirVersion, String name, String extension) {
    String cqlFileLocation =  topic + "/" + fhirVersion + "/files/";
    for (String file : connection.getDirectory(cqlFileLocation)) {
      if (file.startsWith(name) && file.endsWith(extension)) {
        return file;
      }
    }
    logger.info("GitHubFileStore::findGitHubFile(): no files match: " + cqlFileLocation + name + "*.*.*" + extension);
    return null;
  }
}
