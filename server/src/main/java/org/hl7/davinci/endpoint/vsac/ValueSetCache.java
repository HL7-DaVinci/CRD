package org.hl7.davinci.endpoint.vsac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.hl7.davinci.endpoint.database.FhirResource;
import org.hl7.davinci.endpoint.database.FhirResourceCriteria;
import org.hl7.davinci.endpoint.database.FhirResourceRepository;
import org.hl7.davinci.endpoint.vsac.errors.VSACException;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;

public class ValueSetCache {

  public static final String VSAC_TOPIC = "VSACValueSets";

  static final Logger logger = LoggerFactory.getLogger(ValueSetCache.class);

  private FhirResourceRepository fhirResources;

  private File cacheDir;

  private VSACLoader vsacLoader;

  private FhirContext fhirContext;

  public ValueSetCache(String cacheDir) {
    this.fhirContext = ca.uhn.fhir.context.FhirContext.forR4();
    this.initializeLoader();
    this.initializeCacheDir(cacheDir);
  }

  public ValueSetCache(String cacheDir, String username, String password) {
    this.fhirContext = ca.uhn.fhir.context.FhirContext.forR4();
    this.initializeLoader(username, password);
    this.initializeCacheDir(cacheDir);
  }

  private void initializeLoader() {
    String username = System.getenv("VSAC_USERNAME");
    String password = System.getenv("VSAC_PASSWORD");
    if (username == null || password == null) {
      logger.error(
          "VSAC_USERNAME and/or VSAC_PASSWORD not found in environment variables. ValueSetCache will not be able to fetch valuesets.");
    } else {
      initializeLoader(username, password);
    }
  }

  private void initializeLoader(String username, String password) {
    try {
      this.vsacLoader = new VSACLoader(username, password);
      logger.info("VSACLoader sucessfully initialized.");
    } catch (VSACException e) {
      logger.error("Exception setting up VSACLoader. ValueSetCache will not be able to fetch valuesets.", e);
    }
  }

  private void initializeCacheDir(String cachePath) {
    this.cacheDir = new File(cachePath);
    if (cacheDir.exists() && cacheDir.isDirectory()) {
      logger.info("ValueSetCache directory already exists at " + this.cacheDir.getAbsolutePath());
    } else {
      if (this.cacheDir.mkdir()) {
        logger.info("Created ValueSetCache directory at " + this.cacheDir.getAbsolutePath());
      } else {
        logger.error("Failed to create ValueSetCache directory at " + this.cacheDir.getAbsolutePath());
      }
    }
  }

  private void clearLoader() {
    if (this.vsacLoader != null) {
      try {
        this.vsacLoader.close();
      } catch (VSACException ve) {
        logger.warn(ve.getMessage(), ve);
      }
      logger.info("Cleared VSACLoader");
      this.vsacLoader = null;
    }
  }

  public void reinitializeLoader() {
    this.clearLoader();
    this.initializeLoader();
  }

  public void reinitializeLoaderWithCreds(String username, String password) {
    this.clearLoader();
    this.initializeLoader(username, password);
  }

  public boolean fetchValueSet(String oid) {
    // check if the valueset has already been loaded
    FhirResourceCriteria criteria = new FhirResourceCriteria();
    criteria.setFhirVersion("R4")
        .setResourceType("valueset")
        .setId("valueset/" + oid);
    List<FhirResource> fhirResourceList = fhirResources.findById(criteria);

    // Skip fetching if it already has been loaded
    if (!fhirResourceList.isEmpty()) {
      logger.info("ValueSet (" + oid + ") already loaded.");
      return true;
    }

    if (this.vsacLoader == null) {
      return this.fetchValueSetFromCache(oid);
    } else {
      return this.fetchValueSetFromVSAC(oid);
    }
  }

  private boolean fetchValueSetFromCache(String oid) {
    logger.warn("VSACLoader was not setup, possibly due to lack of credentials. ValueSets already in directory will be considered.");
    File valueSetPath = new File(this.cacheDir, "ValueSet-R4-" + oid + ".json");

    try {
      ValueSet valueSet = (ValueSet) this.fhirContext.newJsonParser().parseResource(new FileInputStream(valueSetPath));
      // fix id, for some reason the parser adds 'ValueSet' on it
      valueSet.setId(oid);
      logger.info("ValueSet (" + oid + ") found in cache dir, will use.");
      this.addValueSetToFhirResources(valueSet, valueSetPath);
      return true;
    } catch (FileNotFoundException e) {
      logger.error("ValueSet (" + oid + ") not found in cache dir. It will NOT be available!");
      return false;
    } catch (DataFormatException e) {
      logger.error("ValueSet (" + oid + ") in cache dir is malformed. It will NOT be available!");
      return false;
    }
  }

  private boolean fetchValueSetFromVSAC(String oid) {
    try {
      ValueSet valueSet = vsacLoader.getValueSet(oid);
      File valueSetPath = this.pathForOID(oid);

      if (valueSetPath.exists()) {
        valueSetPath.delete();
        logger.info("Replacing ValueSet (" + oid + ") in cache dir.");
      }

      try {
        FileWriter jsonWriter = new FileWriter(valueSetPath);
        this.fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToWriter(valueSet, jsonWriter);
        jsonWriter.close();
        this.addValueSetToFhirResources(valueSet, valueSetPath);
        return true;
      } catch (IOException ioe) {
        logger.error("Failed to save ValueSet (" + oid + ") to cache dir:", ioe);
        return false;
      }
    } catch(VSACException e) {
      logger.error("Failed to fetch ValueSet (" + oid + "): " + e.getMessage(), e);
      return false;
    }
  }

  private File pathForOID(String oid) {
    return new File(this.cacheDir, "ValueSet-R4-" + oid + ".json");
  }

  private void addValueSetToFhirResources(ValueSet valueSet, File valueSetPath) {
    if (this.fhirResources != null) {
      // create a FhirResource and save it back to the table
      FhirResource fhirResource = new FhirResource();
      fhirResource.setId("valueset/" + valueSet.getId())
          .setFhirVersion("R4")
          .setResourceType("valueset")
          .setTopic(VSAC_TOPIC)
          .setFilename(valueSetPath.getName())
          .setName(valueSet.getName());
      fhirResources.save(fhirResource);
      logger.info("Added ValueSet (" + valueSet.getId() + ") to FhirResourceRepository");
    } else {
      logger.info("Cannot add to FhirResourceRepository, it wasn't provided");
    }
  }

  public void setFhirResources(FhirResourceRepository fhirResources) {
    this.fhirResources = fhirResources;
  }
}