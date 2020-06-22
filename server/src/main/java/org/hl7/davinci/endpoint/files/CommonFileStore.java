package org.hl7.davinci.endpoint.files;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.hl7.ShortNameMaps;
import org.hl7.davinci.endpoint.config.YamlConfig;
import org.hl7.davinci.endpoint.cql.CqlRule;
import org.hl7.davinci.endpoint.database.*;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.vsac.ValueSetCache;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.hateoas.alps.Ext;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public abstract class CommonFileStore implements FileStore {

  static final Logger logger = LoggerFactory.getLogger(CommonFileStore.class);

  @Autowired
  protected RuleFinder ruleFinder;

  @Autowired
  protected RuleMappingRepository lookupTable;

  @Autowired
  protected FhirResourceRepository fhirResources;

  @Autowired
  protected YamlConfig config;

  private ValueSetCache valueSetCache;

  private QuestionnaireValueSetProcessor questionnaireValueSetProcessor;

  private FhirContext ctx;
  private IParser parser;

  public CommonFileStore() {
    this.questionnaireValueSetProcessor = new QuestionnaireValueSetProcessor();
    this.ctx = new org.hl7.davinci.r4.FhirComponents().getFhirContext();
    this.parser = ctx.newJsonParser().setPrettyPrint(true);
  }

  // must define in child class
  public abstract void reload();

  public abstract CqlRule getCqlRule(String topic, String fhirVersion);

  public abstract FileResource getFile(String topic, String fileName, String fhirVersion, boolean convert);

  protected abstract FileResource readFhirResourceFromFile(List<FhirResource> fhirResourceList, String fhirVersion,
      String baseUrl);

  public FileResource getFhirResourceByTopic(String fhirVersion, String resourceType, String name, String baseUrl) {
    logger.info("CommonFileStore::getFhirResourceByTopic(): " + fhirVersion + "/" + resourceType + "/" + name);

    FhirResourceCriteria criteria = new FhirResourceCriteria();
    criteria.setFhirVersion(fhirVersion).setResourceType(resourceType).setName(name);
    List<FhirResource> fhirResourceList = fhirResources.findByName(criteria);
    return readFhirResourceFromFile(fhirResourceList, fhirVersion, baseUrl);
  }

  public FileResource getFhirResourceById(String fhirVersion, String resourceType, String id, String baseUrl) {
    return getFhirResourceById(fhirVersion, resourceType, id, baseUrl, true);
  }

  public FileResource getFhirResourceById(String fhirVersion, String resourceType, String id, String baseUrl,
      boolean isRoot) {
    logger.info("CommonFileStore::getFhirResourceById(): " + fhirVersion + "/" + resourceType + "/" + id);

    FhirResourceCriteria criteria = new FhirResourceCriteria();
    criteria.setFhirVersion(fhirVersion).setResourceType(resourceType).setId(id);
    List<FhirResource> fhirResourceList = fhirResources.findById(criteria);
    FileResource resource = readFhirResourceFromFile(fhirResourceList, fhirVersion, baseUrl);

    // If this is a questionnaire, run it through the processor to modify it before
    // returning.
    // We do not handle nested sub-questionnaire at this time.
    if (isRoot && fhirVersion.equalsIgnoreCase("r4") && resourceType.equalsIgnoreCase("Questionnaire")) {
      String output = assembleQuestionnaire(resource, fhirVersion, baseUrl, isRoot);

      if (output != null) {
        byte[] fileData = output.getBytes(Charset.defaultCharset());
        resource.setResource(new ByteArrayResource(fileData));
      }

      return this.questionnaireValueSetProcessor.processResource(resource, this, baseUrl);
    }

    return resource;
  }

  protected String assembleQuestionnaire(FileResource fileResource, String fhirVersion, String baseUrl, boolean isRoot) {
    logger.info("CommonFileStore::assembleQuestionnaire(): " + fileResource.getFilename());

    this.parser.setParserErrorHandler(new SuppressParserErrorHandler()); // suppress the unknown element warnings

    try {
      InputStream stream = fileResource.getResource().getInputStream();

      if (stream == null)
        return null;

      IBaseResource baseResource = parser.parseResource(stream);

      if (baseResource == null)
        return null;

      Questionnaire q = (Questionnaire) baseResource;

      List<Extension> extensionList = q.getExtension();
      Hashtable<String, org.hl7.fhir.r4.model.Resource> containedList = new Hashtable<String, org.hl7.fhir.r4.model.Resource>();

      for (org.hl7.fhir.r4.model.Resource r : q.getContained()) {
        containedList.put(r.getId(), r);
      }

      int containedSize = containedList.size();

      parseItemList(q.getItem(), fhirVersion, baseUrl, containedList, extensionList);
      
      if (containedSize != containedList.size())
        q.setContained(new ArrayList<org.hl7.fhir.r4.model.Resource>(containedList.values()));

      String output = this.parser.encodeResourceToString(q);
      return output;
    } catch (IOException ex) {
      return null;
    }
  }

  private void parseItemList(List<QuestionnaireItemComponent> itemList, String fhirVersion, String baseUrl,
      Hashtable<String, org.hl7.fhir.r4.model.Resource> containedList, List<Extension> extensionList) {
    if (itemList == null || itemList.size() == 0)
      return;

    for (int i = 0; i < itemList.size();) {
      List<QuestionnaireItemComponent> returnedItemList = 
        parseItem(itemList.get(i), fhirVersion, baseUrl, containedList, extensionList);
      
      if (returnedItemList.size() == 0) {
        continue;
      }

      if (returnedItemList.size() == 1) {
        itemList.set(i, returnedItemList.get(0));
      }
      else {
        itemList.remove(i);
        itemList.addAll(i, returnedItemList);
      }

      i += returnedItemList.size();
    }
  }

  private List<QuestionnaireItemComponent> parseItem(QuestionnaireItemComponent item, String fhirVersion, String baseUrl,
      Hashtable<String, org.hl7.fhir.r4.model.Resource> containedList, List<Extension> extensionList) {
    // find if item has an extension is sub-questionnaire
    Extension e = item.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/sub-questionnaire");

    if (e != null) {
      // read sub questionnaire from file
      CanonicalType value = e.castToCanonical(e.getValue());
      FileResource subFileResource = getFhirResourceById(fhirVersion, "questionnaire", value.asStringValue(), baseUrl,
          false);

      try {
        InputStream stream = subFileResource.getResource().getInputStream();

        if (stream == null)
          return Arrays.asList(item);

        IBaseResource baseResource = parser.parseResource(stream);

        if (baseResource == null)
          return Arrays.asList(item);

        Questionnaire subQuestionnaire = (Questionnaire) baseResource;

        // merge extensions
        for (Extension subExtension : subQuestionnaire.getExtension()) {
          if (extensionList.stream().noneMatch(ext -> ext.equalsDeep(subExtension))) {
            extensionList.add(subExtension);
          }
        }



        // merge contained resources
        for (org.hl7.fhir.r4.model.Resource r : subQuestionnaire.getContained()) {
          containedList.put(r.getId(), r);
        }

        return subQuestionnaire.getItem();
      } catch (IOException ex) {
        // handle if subQuestionniare does not exist
        return Arrays.asList(item);
      }
    }
     
    // parser sub-items
    this.parseItemList(item.getItem(), fhirVersion, baseUrl, containedList, extensionList);

    return Arrays.asList(item);
  }

  public FileResource getFhirResourceByUrl(String fhirVersion, String resourceType, String url, String baseUrl) {
    logger.info("CommonFileStore::getFhirResourceByUrl(): " + fhirVersion + "/" + resourceType + "/" + url);

    FhirResourceCriteria criteria = new FhirResourceCriteria();
    criteria.setFhirVersion(fhirVersion).setResourceType(resourceType).setUrl(url);
    List<FhirResource> fhirResourceList = fhirResources.findByUrl(criteria);
    return readFhirResourceFromFile(fhirResourceList, fhirVersion, baseUrl);
  }

  // from RuleFinder
  public List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria) {
    logger.info("CommonFileStore::findRules(): " + criteria.toString());
    return ruleFinder.findRules(criteria);
  }

  public List<RuleMapping> findAllRules() {
    logger.info("CommonFileStore::findAllRules()");
    return ruleFinder.findAll();
  }

  public List<FhirResource> findAllFhirResources() {
    logger.info("CommonFileStore::findAllFhirResources()");
    return fhirResources.findAll();
  }

  protected void reloadFromFolder(String path) throws IOException {

    File filePath = new File(path);
    if (!filePath.exists()) {
      String error = "file path " + path + " does not exist";
      throw new IOException(error);
    }

    File[] topics = new File(path).listFiles();
    for (File topic : topics) {
      if (topic.isDirectory()) {

        String topicName = topic.getName();

        // skip the shared folder for now...
        if (topicName.equalsIgnoreCase(FileStore.SHARED_TOPIC)) {
          logger.info("  CommonFileStore::reloadFromFolder() found Shared files");

          File[] fhirFolders = topic.listFiles();
          for (File fhirFolder : fhirFolders) {
            if (fhirFolder.isDirectory()) {
              String fhirVersion = fhirFolder.getName();
              processFhirFolder(topicName, fhirVersion, fhirFolder);
            }
          }

        } else if (topicName.startsWith(".")) {
          // logger.info(" CommonFileStore::reloadFromFolder() skipping all folders
          // starting with .: " + topicName);
        } else {
          logger.info("  CommonFileStore::reloadFromFolder() found topic: " + topicName);

          // process the metadata file
          File[] fhirFolders = topic.listFiles();
          for (File file : fhirFolders) {
            String fileName = file.getName();
            if (fileName.equalsIgnoreCase("TopicMetadata.json")) {
              ObjectMapper objectMapper = new ObjectMapper();

              try {
                // read the file
                String content = new String(Files.readAllBytes(file.toPath()));

                // convert to object
                TopicMetadata metadata = objectMapper.readValue(content, TopicMetadata.class);

                for (Mapping mapping : metadata.getMappings()) {
                  for (String code : mapping.getCodes()) {
                    for (String payer : metadata.getPayers()) {
                      for (String fhirVersion : metadata.getFhirVersions()) {

                        String mainCqlLibraryName = metadata.getTopic() + "Rule";
                        File mainCqlFile = findFile(path, metadata.getTopic(), fhirVersion, mainCqlLibraryName, FileStore.CQL_EXTENSION);
                        if (mainCqlFile == null) {
                          logger.warn("CommonFileStore::reloadFromFolder(): failed to find main CQL file for topic: "
                              + metadata.getTopic());
                        } else {
                          logger.info("    Added: " + metadata.getTopic() + ": " + payer + ", "
                              + mapping.getCodeSystem() + ", " + code + " (" + fhirVersion + ")");

                          // create table entry and store it back to the table
                          RuleMapping ruleMappingEntry = new RuleMapping();
                          ruleMappingEntry.setPayer(ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.get(payer))
                              .setCodeSystem(
                                  ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.get(mapping.getCodeSystem()))
                              .setCode(code).setFhirVersion(fhirVersion).setTopic(metadata.getTopic())
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
            } else {
              if (file.isDirectory()) {
                String fhirVersion = fileName;
                processFhirFolder(topicName, fhirVersion, file);
              }
            }
          }
        }
      }
    }

    /*
     * uncomment to print contents of FhirResource table on reload // loop through
     * the fhir resources table and print it out logger.info("FhirResource: " +
     * FhirResource.getColumnsString()); for (FhirResource resource :
     * fhirResources.findAll()) { logger.info(resource.toString()); }
     */

  }

  private void processFhirFolder(String topic, String fhirVersion, File fhirPath) {
    fhirVersion = fhirVersion.toUpperCase();
    logger.info("      CommonFileStore::processFhirFolder(): " + fhirVersion + ": " + fhirPath.getName());

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

    File[] directories = fhirPath.listFiles();
    for (File folder : directories) {
      if (folder.getName().equalsIgnoreCase("resources") && folder.isDirectory()) {

        File[] resources = folder.listFiles();
        for (File resource : resources) {
          if (resource.isFile()) {
            String filename = resource.getName();
            logger.info("        process: FHIR Resource: " + filename);

            String[] parts = filename.split("-");
            if (parts.length > 2) {

              if (!parts[1].equalsIgnoreCase(fhirVersion)) {
                logger.warn("CommonFileStore::processFhirFolder() warning: FhirVersion doesn't match!");
                continue;
              }

              // parse the the resource file into the correct FHIR
              IBaseResource baseResource = null;
              try {
                baseResource = parser.parseResource(new FileInputStream(resource));
              } catch (FileNotFoundException e) {
                logger.warn("could not find file: " + resource.getPath());
                continue;
              }

              processFhirResource(baseResource, filename, filename, fhirVersion, topic);
            }
          }
        }
      }
    }
  }

  protected void processFhirResource(IBaseResource baseResource, String path, String filename, String fhirVersion, String topic) {
    String resourceType;
    String resourceId = "";
    String resourceName = "";
    String resourceUrl = null;

    resourceType = baseResource.fhirType(); // grab the FHIR resource type out of the resource
    resourceType = resourceType.toLowerCase();

    if (fhirVersion.equalsIgnoreCase("R4")) {
      if (resourceType.equalsIgnoreCase("Questionnaire")) {
        org.hl7.fhir.r4.model.Questionnaire questionnaire = (org.hl7.fhir.r4.model.Questionnaire) baseResource;
        resourceId = questionnaire.getId();
        resourceName = questionnaire.getName();
        resourceUrl = questionnaire.getUrl();
        findAndFetchRequiredVSACValueSets(questionnaire);
      } else if (resourceType.equalsIgnoreCase("Library")) {
        org.hl7.fhir.r4.model.Library library = (org.hl7.fhir.r4.model.Library) baseResource;
        resourceId = library.getId();
        resourceName = library.getName();
        resourceUrl = library.getUrl();
        // Look at data requirements for value sets
        findAndFetchRequiredVSACValueSets(library);
      } else if (resourceType.equalsIgnoreCase("ValueSet")) {
        org.hl7.fhir.r4.model.ValueSet valueSet = (org.hl7.fhir.r4.model.ValueSet) baseResource;
        resourceId = "ValueSet/" + valueSet.getIdElement().getIdPart();
        resourceName = valueSet.getName();
        resourceUrl = valueSet.getUrl();
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
        resourceId = "ValueSet/" + valueSet.getIdElement().getIdPart();
        resourceName = valueSet.getName();
        resourceUrl = valueSet.getUrl();
      }
    }

    if (resourceId == null) {
      // this should never happen, there should always be an ID
      logger.error("Could not find ID for: " + filename + ", defaulting to '" + filename + "' as the ID");
      resourceId = filename;
    }

    if (resourceName == null) {
      resourceName = stripNameFromResourceFilename(filename, fhirVersion);
      logger.info(
          "Could not find name for: " + filename + ", defaulting to '" + resourceName + "' as the name");
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
        .setPath(path)
        .setName(resourceName);
    if (resourceUrl != null) {
      fhirResource.setUrl(resourceUrl);
    }
    fhirResources.save(fhirResource);
  }

  /**
   * Called by the DataController to ensure we have a fresh VSACLoader for getting
   * value sets before starting the reloading process.
   */
  public void reinitializeVSACLoader() {
    this.getValueSetCache().reinitializeLoader();
  }

  /**
   * Called by the DataController to ensure we have a fresh VSACLoader for getting
   * value sets before starting the reloading process.
   * 
   * @param username VSAC/UMLS Username
   * @param password VSAC/UMLS Password
   */
  public void reinitializeVSACLoader(String username, String password) {
    this.getValueSetCache().reinitializeLoaderWithCreds(username, password);
  }

  /**
   * Gets or sets up and returns the ValueSetCache. The setup code provides the
   * FhirResourceRepository to the ValueSetCache so it is able add the fetched
   * value sets to the repository.
   * 
   * @return The ValueSetCache to use for getting ValueSets.
   */
  protected ValueSetCache getValueSetCache() {
    if (this.valueSetCache == null) {
      this.valueSetCache = new ValueSetCache(this.config.getValueSetCachePath());
      this.valueSetCache.setFhirResources(this.fhirResources);
    }
    return this.valueSetCache;
  }

  /**
   * Looks for ValueSet references in Library.dataRequirement.codeFilter entries
   * that point to a VSAC ValueSet by OID and have the cache fetch the ValueSet.
   * 
   * @param library The FHIR Library resource to look for ValueSet references in.
   */
  protected void findAndFetchRequiredVSACValueSets(org.hl7.fhir.r4.model.Library library) {
    for (org.hl7.fhir.r4.model.DataRequirement dataReq : library.getDataRequirement()) {
      for (org.hl7.fhir.r4.model.DataRequirement.DataRequirementCodeFilterComponent codeFilter : dataReq
          .getCodeFilter()) {
        String valueSetRef = codeFilter.getValueSet();
        if (valueSetRef.startsWith(ValueSetCache.VSAC_CANONICAL_BASE)) {
          String valueSetId = valueSetRef.split("ValueSet/")[1];
          logger.info("          VSAC ValueSet reference found: " + valueSetId);
          this.getValueSetCache().fetchValueSet(valueSetId);
        }
      }
    }
  }

  /**
   * Looks for ValueSet references in Questionnaire.item**.answerValueSet entries that point to a VSAC ValueSet by OID and have the cache fetch the
   * ValueSet.
   * 
   * @param questionnaire The FHIR Questionnaire resource to look for ValueSet references in.
   */
  protected void findAndFetchRequiredVSACValueSets(org.hl7.fhir.r4.model.Questionnaire questionnaire) {
    findAndFetchRequiredVSACValueSets(questionnaire.getItem());
  }

  /**
   * Looks for ValueSet references in a list of Questionnaire item components in the answerValueSet entries that
   * point to a VSAC ValueSet by OID and have the cache fetch the ValueSet. Also recurses into children item elements.
   * 
   * @param itemComponents The FHIR Questionnaire Item components to look for ValueSet references in.
   */
  protected void findAndFetchRequiredVSACValueSets(List<org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent> itemComponents) {
    for (org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent itemComponent : itemComponents) {
      // If there is an answerValueSet field we should see if it is a VSAC reference
      if (itemComponent.hasAnswerValueSet()) {
        String valueSetRef = itemComponent.getAnswerValueSet();
        if (valueSetRef.startsWith(ValueSetCache.VSAC_CANONICAL_BASE)) {
          String valueSetId = valueSetRef.split("ValueSet/")[1];
          logger.info("          VSAC ValueSet reference found: " + valueSetId);
          this.getValueSetCache().fetchValueSet(valueSetId);
        }
      }

      // Recurse down into child items.
      if (itemComponent.hasItem()) {
        findAndFetchRequiredVSACValueSets(itemComponent.getItem());
      }
    }
  }

  protected File findFile(String localPath, String topic, String fhirVersion, String name, String extension) {
    String cqlFileLocation = localPath + topic + "/" + fhirVersion + "/files/";
    File dir = new File(cqlFileLocation);
    String regex = name + "-\\d.\\d.\\d" + extension;
    FileFilter fileFilter = new RegexFileFilter(regex);
    File[] files = dir.listFiles(fileFilter);
    if (files.length > 0) {
      // just return the first one
      return files[0];
    }
    logger.info("CommonFileStore::findFile(): no files match: " + cqlFileLocation + regex);
    return null;
  }

  protected String stripNameFromResourceFilename(String filename, String fhirVersion) {
    // example filename: Library-R4-HomeOxygenTherapy-prepopulation.json
    int fhirIndex = filename.toUpperCase().indexOf(fhirVersion.toUpperCase());
    int startIndex = fhirIndex + fhirVersion.length() + 1;
    int extensionIndex = filename.toUpperCase().indexOf(".json".toUpperCase());
    return filename.substring(startIndex, extensionIndex);
  }
}
