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

/**
 * Manages the ValueSetCache folder. This is used by the FileStore classes. Forwards requests to an instance of VSACLoader to fetch valuesets
 * when they are requested. If the VSACLoader could not be set up it will look for ValueSets in the cache folder.
 */
public class ValueSetCache {

  /**
   * Topic to use when storing information about valuesets to the FhirResourceRepository.
   */
  public static final String VSAC_TOPIC = "VSACValueSets";

  public static final String VSAC_CANONICAL_BASE = "http://cts.nlm.nih.gov/fhir/ValueSet/";

  static final Logger logger = LoggerFactory.getLogger(ValueSetCache.class);

  private FhirResourceRepository fhirResources;

  private File cacheDir;

  /**
   * Currently initialized VSACLoader. This may be null if there are no credentials found.
   */
  private VSACLoader vsacLoader;

  /**
   * Used for parsing and writing JSON FHIR ValueSets.
   */
  private FhirContext fhirContext;

  /**
   * Initializes the cache with no credentials passed in. Note. initializeLoader function may find credentials in the environement variables.
   * 
   * @param cacheDir Location of the ValueSet cache folder.
   */
  public ValueSetCache(String cacheDir) {
    this.fhirContext = ca.uhn.fhir.context.FhirContext.forR4();
    this.initializeLoader();
    this.initializeCacheDir(cacheDir);
  }

  /**
   * Initializes the cache with credentials passed in.
   * 
   * @param cacheDir Location of the ValueSet cache folder.
   * @param apiKey UMLS/VSAC API KEY
   */
  public ValueSetCache(String cacheDir, String apiKey) {
    this.fhirContext = ca.uhn.fhir.context.FhirContext.forR4();
    this.initializeLoader(apiKey);
    this.initializeCacheDir(cacheDir);
  }

  /**
   * Initalizes the VSACLoader if it finds credentials on the evironment variables at VSAC_USERNAME and VSAC_PASSWORD.
   */
  private void initializeLoader() {
    String apiKey = System.getenv("VSAC_API_KEY");
    if (apiKey == null) {
      logger.error(
          "VSAC_API_KEY not found in environment variables. ValueSetCache will not be able to fetch valuesets.");
    } else {
      initializeLoader(apiKey);
    }
  }

  /**
   * Initializes the VSACLoader with provided credentials.
   * 
   * @param apiKey UMLS/VSAC API KEY
   */
  private void initializeLoader(String apiKey) {
    try {
      this.vsacLoader = new VSACLoader(apiKey);
      logger.info("VSACLoader sucessfully initialized.");
    } catch (VSACException e) {
      logger.error("Exception setting up VSACLoader. ValueSetCache will not be able to fetch valuesets.", e);
    }
  }

  /**
   * Creates the cache folder if it doesn't exist.
   * 
   * @param cachePath Path to the ValueSet cache folder.
   */
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

  /**
   * Wipe out the VSACLoader. This should be done before each reload of rulesets because the Ticket Granting Tickets are not eternal.
   */
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

  /**
   * Reinitializes the loader if possible using environment variables.
   */
  public void reinitializeLoader() {
    this.clearLoader();
    this.initializeLoader();
  }

  /**
   * Reinitializes the VSACLoader with provided credentials.
   * 
   * @param apiKey UMLS/VSAC API KEY
   */
  public void reinitializeLoaderWithCreds(String apiKey) {
    this.clearLoader();
    this.initializeLoader(apiKey);
  }

  /**
   * Fetch a ValueSet from VSAC or cache and add it to the FhirResourceRepository.
   * @param oid The VSAC OID of the ValueSet to fetch.
   * @return true if sucessful, false if failed to fetch ValueSet.
   */
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

    // If the VSACLoader is initialized, attempt to fetch from VSAC. Otherwise fall back to cache dir.
    if (this.vsacLoader == null) {
      return this.fetchValueSetFromCache(oid);
    } else {
      return this.fetchValueSetFromVSAC(oid);
    }
  }

  /**
   * Fetches a ValueSet already in the cache dir and adds it to the FhirResourceRepository.
   * 
   * @param oid The VSAC OID of the ValueSet to fetch.
   * @return true if sucessfuly found in cache dir. Otherwise, false.
   */
  private boolean fetchValueSetFromCache(String oid) {
    logger.warn("            VSACLoader was not setup, possibly due to lack of credentials. ValueSets already in directory will be considered.");
    File valueSetPath = new File(this.cacheDir, "ValueSet-R4-" + oid + ".json");

    try {
      ValueSet valueSet = (ValueSet) this.fhirContext.newJsonParser().parseResource(new FileInputStream(valueSetPath));
      // fix id, for some reason the parser adds 'ValueSet' on it
      valueSet.setId(oid);
      logger.info("            ValueSet (" + oid + ") found in cache dir, will use.");
      this.addValueSetToFhirResources(valueSet, valueSetPath);
      return true;
    } catch (FileNotFoundException e) {
      logger.error("            ValueSet (" + oid + ") not found in cache dir. It will NOT be available!");
      return false;
    } catch (DataFormatException e) {
      logger.error("            ValueSet (" + oid + ") in cache dir is malformed. It will NOT be available!");
      return false;
    }
  }

  /**
   * Fetches a ValueSet from VSAC and adds it to the FhirResourceRepository. 
   * 
   * @param oid The VSAC OID of the ValueSet to fetch.
   * @return true if sucessfully fetched. Otherwise, false.
   */
  private boolean fetchValueSetFromVSAC(String oid) {
    try {
      ValueSet valueSet = vsacLoader.getValueSet(oid);
      File valueSetPath = this.pathForOID(oid);

      if (valueSetPath.exists()) {
        valueSetPath.delete();
        logger.info("            Replacing ValueSet (" + oid + ") in cache dir.");
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

  /**
   * Helper function to turn a ValueSet oid into to the expected path of the ValueSet in the cache dir.
   * 
   * @param oid The VSAC OID of the ValueSet.
   * @return Path to the expected location.
   */
  private File pathForOID(String oid) {
    return new File(this.cacheDir, "ValueSet-R4-" + oid + ".json");
  }

  /**
   * Add a ValueSet to the FhirResourcesRepository.
   * 
   * @param valueSet The ValueSet to add.
   * @param valueSetPath The path to the JSON file for this value set in the cache folder.
   */
  private void addValueSetToFhirResources(ValueSet valueSet, File valueSetPath) {
    if (this.fhirResources != null) {
      // create a FhirResource and save it back to the table
      FhirResource fhirResource = new FhirResource();
      fhirResource.setId(valueSet.getId())
          .setFhirVersion("R4")
          .setResourceType("valueset")
          .setTopic(VSAC_TOPIC)
          .setFilename(valueSetPath.getName())
          .setName(valueSet.getName())
          .setUrl(valueSet.getUrl());
      fhirResources.save(fhirResource);
      logger.info("            Added ValueSet (" + valueSet.getId() + ") to FhirResourceRepository");
    } else {
      logger.info("            Cannot add to FhirResourceRepository, it wasn't provided");
    }
  }

  /**
   * Used to set the FhirResourceRepository before reloading rulesets.
   * 
   * @param fhirResources
   */
  public void setFhirResources(FhirResourceRepository fhirResources) {
    this.fhirResources = fhirResources;
  }
}