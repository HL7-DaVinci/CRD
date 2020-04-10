package org.hl7.davinci.endpoint.vsac;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.hl7.davinci.endpoint.config.YamlConfig;
import org.hl7.davinci.endpoint.database.FhirResource;
import org.hl7.davinci.endpoint.database.FhirResourceRepository;
import org.hl7.davinci.endpoint.vsac.errors.VSACException;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.LoggerFactory;

public class ValueSetCache {

  public static final String VSAC_TOPIC = "VSACValueSets";

  static final Logger logger = LoggerFactory.getLogger(ValueSetCache.class);

  private FhirResourceRepository fhirResources;

  private File cacheDir;

  private VSACLoader vsacLoader;

  public ValueSetCache(String cacheDir) {
    String username = System.getenv("VSAC_USERNAME");
    String password = System.getenv("VSAC_PASSWORD");
    if (username == null || password == null) {
      logger.error(
          "VSAC_USERNAME and/or VSAC_PASSWORD not found in environment variables. ValueSetCache will not be able to fetch valuesets.");
    } else {
      initalizeLoader(username, password);
    }
    this.initalizeCacheDir(cacheDir);
  }

  public ValueSetCache(String cacheDir, String username, String password) {
    this.initalizeLoader(username, password);
    this.initalizeCacheDir(cacheDir);
  }

  private void initalizeLoader(String username, String password) {
    try {
      this.vsacLoader = new VSACLoader(username, password);
    } catch (VSACException e) {
      logger.error("Exception setting up VSACLoader. ValueSetCache will not be able to fetch valuesets.", e);
    }
  }

  private void initalizeCacheDir(String cachePath) {
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

  public boolean fetchValueSet(String oid) {
    if (this.vsacLoader == null) {
      return this.fetchValueSetFromCache(oid);
    } else {
      return this.fetchValueSetFromVSAC(oid);
    }
  }

  private boolean fetchValueSetFromCache(String oid) {
    logger.warn("VSACLoader was not setup, possibly due to lack of credentials. ValueSets already in directory will be considered.");
    File valueSetPath = new File(this.cacheDir, oid + ".json");
    if (valueSetPath.exists()) {
      // todo add to repository
      return true;
    } else {
      logger.error("ValueSet (" + oid + ") not found in cache dir.");
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
        ca.uhn.fhir.context.FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToWriter(valueSet, jsonWriter);
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