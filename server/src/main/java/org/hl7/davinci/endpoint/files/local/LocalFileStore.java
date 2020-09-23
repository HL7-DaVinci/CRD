package org.hl7.davinci.endpoint.files.local;

import org.apache.commons.io.FilenameUtils;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.cql.CqlRule;
import org.hl7.davinci.endpoint.database.*;
import org.hl7.davinci.endpoint.files.*;
import org.hl7.davinci.endpoint.vsac.ValueSetCache;
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

    try {
      reloadFromFolder(path);
    } catch (IOException e) {
      logger.error("FATAL ERROR: Failed to reload from folder: " + e.getMessage());
      System.exit(1);
    }

    // Load the examples folder
    String examplesPath = config.getLocalDb().getExamplesPath();
    logger.info("LocalFileStore::reload(): (examples) " + examplesPath);

    try {
      reloadFromFolder(examplesPath);
    } catch (IOException e) {
      logger.error("FATAL ERROR: Failed to reload examples from folder: " + e.getMessage());
      System.exit(1);
    }

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
    String examplesPath = config.getLocalDb().getExamplesPath();

    String mainCqlLibraryName = topic + "Rule";
    File mainCqlFile = findFile(localPath, topic, fhirVersion, mainCqlLibraryName, FileStore.CQL_EXTENSION);

    // look for the main cql file in the examples path as well
    if (mainCqlFile == null) {
      mainCqlFile = findFile(examplesPath, topic, fhirVersion, mainCqlLibraryName, FileStore.CQL_EXTENSION);
    }
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

    File helperCqlFile = findFile(localPath, FileStore.SHARED_TOPIC, fhirVersion, FileStore.FHIR_HELPERS_FILENAME, FileStore.CQL_EXTENSION);

    // look for the helper cql file in the examples path as well
    if (helperCqlFile == null) {
      helperCqlFile = findFile(examplesPath, FileStore.SHARED_TOPIC, fhirVersion, FileStore.FHIR_HELPERS_FILENAME, FileStore.CQL_EXTENSION);
    }
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
    String partialFilePath = topic + "/" + fhirVersion + "/files/" + fileName;
    String filePath = localPath + partialFilePath;
    File file = new File(filePath);

    if (!Files.exists(file.toPath())) {
      logger.info("LocalFileStore::getFile(): could not find file: " + file.toString() + " will try examples folder");
      String examplesPath = config.getLocalDb().getExamplesPath();
      filePath = examplesPath + partialFilePath;
      file = new File(filePath);
      if (!Files.exists(file.toPath())) {
        logger.warn("LocalFileStore::getFile(): could not find file: " + file.toString());
        return null;
      }
    }

    byte[] fileData = null;
    try {
      fileData = Files.readAllBytes(file.toPath());

      // convert to ELM
      if (convert && FilenameUtils.getExtension(fileName).toUpperCase().equals("CQL")) {
        logger.info("LocalFileStore::getFile() converting CQL to JSON ELM");
        String cql = new String(fileData);
        try {
          String elm = CqlExecution.translateToElm(cql, new CDSLibrarySourceProvider(this));
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

  protected String readFhirResourceFromFile(FhirResource fhirResource, String fhirVersion) {
    String fileString = null;
    String filePath;
    String localPath = config.getLocalDb().getPath();
    File file = null;

    // If the topic indicates it's actually from the ValueSet cache. Grab file path from there.
    if (fhirResource.getTopic().equals(ValueSetCache.VSAC_TOPIC)) {
      filePath = config.getValueSetCachePath() + fhirResource.getFilename();
      file = new File(filePath);
      logger.warn("Atempting to serve valueset from cache at: " + filePath);
    } else {
      String partialFilePath = fhirResource.getTopic() + "/" + fhirVersion + "/resources/" + fhirResource.getFilename();
      filePath = localPath + partialFilePath;
      file = new File(filePath);

      if (!Files.exists(file.toPath())) {
        logger.info("LocalFileStore::readFhirResourceFromFile(): could not find file: " + file.toString() + " will try examples folder");
        String examplesPath = config.getLocalDb().getExamplesPath();
        filePath = examplesPath + partialFilePath;
        file = new File(filePath);
        if (!Files.exists(file.toPath())) {
          logger.warn("LocalFileStore::readFhirResourceFromFile(): could not find file: " + file.toString());
          return null;
        }
      }

      logger.warn("Attemping to serve file from: " + filePath);
    }

    try {
      byte[] fileData = Files.readAllBytes(file.toPath());
      fileString = new String(fileData, Charset.defaultCharset());
    } catch (IOException e) {
      logger.warn("LocalFileStore::readFhirResourceFromFile() failed to get file: " + e.getMessage());
      return null;
    }

    return fileString;
  }

  private class CqlLibrarySourceProvider implements LibrarySourceProvider {

    @Override
    public InputStream getLibrarySource(org.hl7.elm.r1.VersionedIdentifier libraryIdentifier) {
      // TODO Auto-generated method stub
      return null;
    }

  }
}

