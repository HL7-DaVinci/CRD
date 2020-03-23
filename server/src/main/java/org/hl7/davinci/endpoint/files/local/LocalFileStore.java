package org.hl7.davinci.endpoint.files.local;

import org.apache.commons.io.FilenameUtils;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.cql.CqlRule;
import org.hl7.davinci.endpoint.database.*;
import org.hl7.davinci.endpoint.files.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;


@Component
@Profile("localDb")
public class LocalFileStore extends CommonFileStore {

  static final Logger logger = LoggerFactory.getLogger(LocalFileStore.class);


  @Autowired
  public LocalFileStore() {
    logger.info("Using LocalFileStore");
  }

  public void reload() {
    long startTime = System.nanoTime();

    // clear the database first
    lookupTable.deleteAll();
    fhirResources.deleteAll();

    String path = config.getLocalDb().getPath();
    logger.info("LocalFileStore::reload(): " + path);

    reloadFromFolder(path);

    long endTime = System.nanoTime();
    long timeElapsed = endTime - startTime;
    float seconds = (float)timeElapsed / (float)1000000000;

    logger.info("LocalFileStore::reload(): completed in " + seconds + " seconds");
  }

  public CqlRule getCqlRule(String topic, String fhirVersion) {
    logger.info("LocalFileStore::getCqlRule(): " + topic + "/" + fhirVersion);

    // load CQL files needed for the CRD Rule
    HashMap<String, byte[]> cqlFiles = new HashMap<>();
    String localPath = config.getLocalDb().getPath();

    String mainCqlLibraryName = topic + "Rule";
    File mainCqlFile = findFile(localPath, topic, fhirVersion, mainCqlLibraryName, ".cql");
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

    File helperCqlFile = findFile(localPath, "Shared", fhirVersion, "FHIRHelpers", ".cql");
    if (helperCqlFile == null) {
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

  protected FileResource readFhirResourceFromFile(List<FhirResource> fhirResourceList, String fhirVersion, String baseUrl) {
    byte[] fileData = null;

    if (fhirResourceList.size() > 0) {
      // just return the first matched resource
      FhirResource fhirResource = fhirResourceList.get(0);

      String localPath = config.getLocalDb().getPath();
      String filePath = localPath + fhirResource.getTopic() + "/" + fhirVersion + "/resources/" + fhirResource.getFilename();
      File file = new File(filePath);
      try {
        fileData = Files.readAllBytes(file.toPath());

        // replace <server-path> with the proper path
        //String fullLaunchUrl = config.getLaunchUrl().toString();
        //String baseUrl = fullLaunchUrl.substring(0, fullLaunchUrl.indexOf(config.getLaunchUrl().getPath())+1);
        String partialUrl = baseUrl + "fhir/" + fhirVersion + "/";

        String fileString = new String(fileData, Charset.defaultCharset());
        fileString = fileString.replace("<server-path>", partialUrl);
        fileData = fileString.getBytes(Charset.defaultCharset());

        FileResource fileResource = new FileResource();
        fileResource.setFilename(fhirResource.getFilename());
        fileResource.setResource(new ByteArrayResource(fileData));
        return fileResource;

      } catch (IOException e) {
        logger.warn("LocalFileStore::getFhirResourceByTopic() failed to get file: " + e.getMessage());
        return null;
      }

    } else {
      return null;
    }
  }
}

