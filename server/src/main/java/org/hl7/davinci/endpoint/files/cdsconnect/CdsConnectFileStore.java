package org.hl7.davinci.endpoint.files.cdsconnect;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.ShortNameMaps;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.config.YamlConfig;
import org.hl7.davinci.endpoint.cql.CqlRule;
import org.hl7.davinci.endpoint.database.*;
import org.hl7.davinci.endpoint.files.*;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.vsac.ValueSetCache;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Resource;
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
import java.util.stream.Collectors;

@Component
@Profile("cdsConnect")
public class CdsConnectFileStore implements FileStore {

  static final Logger logger = LoggerFactory.getLogger(CdsConnectFileStore.class);

  @Autowired
  private CdsConnectConnection connection;

  @Autowired
  private RuleFinder ruleFinder;

  @Autowired
  protected RuleMappingRepository lookupTable;

  @Autowired
  protected FhirResourceRepository fhirResources;

  @Autowired
  protected YamlConfig config;

  private ValueSetCache valueSetCache;

  private QuestionnaireValueSetProcessor questionnaireValueSetProcessor;


  //TODO: reorganize the parser to maybe support STU3
  private FhirContext ctx;
  private IParser parser;

  public CdsConnectFileStore() {
    logger.info("Using CdsConnectFileStore");
    this.questionnaireValueSetProcessor = new QuestionnaireValueSetProcessor();
    this.ctx = new org.hl7.davinci.r4.FhirComponents().getFhirContext();
    this.parser = ctx.newJsonParser().setPrettyPrint(true);
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

          for (String fhirVersion : metadata.getFhirVersions()) {

            logger.info("    Added: " + metadata.getTopic() + ": (" + fhirVersion + ")");

            // create table entry and store it back to the table
            RuleMapping ruleMappingEntry = new RuleMapping();
            ruleMappingEntry.setPayer("")
                .setCodeSystem("")
                .setCode("")
                .setFhirVersion(fhirVersion)
                .setTopic(metadata.getTopic())
                .setRuleFile(mainCqlFile)
                .setNode(artifact.getId());
            lookupTable.save(ruleMappingEntry);
          }

        } else {
          logger.info("  CdsConnectFileStore::reload() found topic: " + topic);

          String mainCqlLibraryName = metadata.getTopic() + "Rule";
          String mainCqlFile = findFile(files, mainCqlLibraryName, FileStore.CQL_EXTENSION);

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
                        .setRuleFile(mainCqlFile)
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
      }


      // process the fhir resource files
      // setup the proper FHIR Context for the version of FHIR we are dealing with
      FhirContext r4ctx = new org.hl7.davinci.r4.FhirComponents().getFhirContext();
      IParser r4parser = r4ctx.newJsonParser();
      r4parser.setParserErrorHandler(new SuppressParserErrorHandler()); // suppress the unknown element warnings

      FhirContext stu3ctx = new org.hl7.davinci.stu3.FhirComponents().getFhirContext();
      IParser stu3parser = stu3ctx.newJsonParser();
      stu3parser.setParserErrorHandler(new SuppressParserErrorHandler()); // suppress the unknown element warnings


      for (CdsConnectFile file : files) {
        String path = file.getPath();
        String filename = file.getFilename();

        if (filename.endsWith(".json")) {
          logger.info("        process: FHIR Resource: " + filename);

          String[] parts = filename.split("-");

          String resourceType = parts[0];
          String fhirVersion = parts[1];
          String name = parts[2];

          byte[] fileContents = file.getCqlBundle();
          String resourceId = "";
          String resourceName = "";
          String resourceUrl = null;

          // parse the the resource file into the correct FHIR resource
          if (fhirVersion.equalsIgnoreCase("R4")) {
            IBaseResource baseResource = r4parser.parseResource(new ByteArrayInputStream(fileContents));
            resourceType = baseResource.fhirType(); // grab the FHIR resource type out of the resource
            resourceType = resourceType.toLowerCase();

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
            IBaseResource baseResource = stu3parser.parseResource(new ByteArrayInputStream(fileContents));
            resourceType = baseResource.fhirType(); // grab the FHIR resource type out of the resource
            resourceType = resourceType.toLowerCase();

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
              .setFilename(path)
              .setName(resourceName);
          if (resourceUrl != null) {
            fhirResource.setUrl(resourceUrl);
          }
          fhirResources.save(fhirResource);

        }
      }
    }


    //uncomment to print contents of FhirResource table on reload
    // loop through the fhir resources table and print it out
    logger.info("FhirResource: " + FhirResource.getColumnsString());
    for (FhirResource resource : fhirResources.findAll()) {
      logger.info(resource.toString());
    }


    long endTime = System.nanoTime();
    long timeElapsed = endTime - startTime;
    float seconds = (float) timeElapsed / (float) 1000000000;

    if (success) {
      logger.info("CdsConnectFileStore::reload(): completed in " + seconds + " seconds");
    } else {
      logger.warn("CdsConnectFileStore::reload(): failed in " + seconds + " seconds");
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
    CdsConnectFile file = new CdsConnectFile(connection, rule.getRuleFile());
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
    CdsConnectFile sharedFile = new CdsConnectFile(connection, sharedRule.getRuleFile());
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

    Optional<CdsConnectFile> foundFile = files.stream()
        .filter(f -> f.getFilename().equalsIgnoreCase(fileName))
        .findFirst();

    if (!foundFile.isPresent()) {
      logger.info("CdsConnectFileStore: getFile(): matching file could not be found");
      return null;
    } else {

      // read the file
      byte[] fileData = foundFile.get().getCqlBundle();

      // convert to ELM
      if (convert && FilenameUtils.getExtension(fileName).toUpperCase().equals("CQL")) {
        logger.info("CdsConnectFileStore::getFile() converting CQL to JSON ELM");

        // convert byte array to string
        String cql = new String(fileData);
        byte[] elmFileData = null;
        try {
          String elm = CqlExecution.translateToElm(cql);
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

  protected FileResource readFhirResourceFromFile(List<FhirResource> fhirResourceList, String fhirVersion, String baseUrl) {
    byte[] fileData = null;

    if (fhirResourceList.size() > 0) {
      // just return the first matched resource
      FhirResource fhirResource = fhirResourceList.get(0);

      String filePath;
      InputStream inputStream;
      String fileString;

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

        CdsConnectFile file = new CdsConnectFile(connection, fhirResource.getFilename());
        fileData = file.getCqlBundle();
        fileString = new String(fileData);

      }

      // replace <server-path> with the proper path
      String partialUrl = baseUrl + "fhir/" + fhirVersion + "/";

      fileString = fileString.replace("<server-path>", partialUrl);
      fileData = fileString.getBytes(Charset.defaultCharset());

      FileResource fileResource = new FileResource();
      fileResource.setFilename(fhirResource.getFilename());
      fileResource.setResource(new ByteArrayResource(fileData));
      return fileResource;

    } else {
      return null;
    }
  }

  public FileResource getFhirResourceByTopic(String fhirVersion, String resourceType, String name, String baseUrl) {
    logger.info("CdsConnectFileStore::getFhirResourceByTopic(): " + fhirVersion + "/" + resourceType + "/" + name);
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
    logger.info("CdsConnectFileStore::getFhirResourceById(): " + fhirVersion + "/" + resourceType + "/" + id);

    FhirResourceCriteria criteria = new FhirResourceCriteria();
    criteria.setFhirVersion(fhirVersion).setResourceType(resourceType).setId(id);
    List<FhirResource> fhirResourceList = fhirResources.findById(criteria);
    FileResource resource = readFhirResourceFromFile(fhirResourceList, fhirVersion, baseUrl);

    //TODO: need to reorganize the sub questionnaire assembly

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
  //TODO: need to move the sub questionnaire stuff into a common place with CommonFileStore

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
      Hashtable<String, Resource> containedList = new Hashtable<String, org.hl7.fhir.r4.model.Resource>();

      for (org.hl7.fhir.r4.model.Resource r : q.getContained()) {
        containedList.put(r.getId(), r);
      }

      int containedSize = containedList.size();

      parseItemList(q.getItem(), fhirVersion, baseUrl, containedList, extensionList);

      if (containedSize != containedList.size())
        q.setContained(new ArrayList<Resource>(containedList.values()));

      String output = this.parser.encodeResourceToString(q);
      return output;
    } catch (IOException ex) {
      return null;
    }
  }

  private void parseItemList(List<Questionnaire.QuestionnaireItemComponent> itemList, String fhirVersion, String baseUrl,
                             Hashtable<String, org.hl7.fhir.r4.model.Resource> containedList, List<Extension> extnesionList) {
    if (itemList == null || itemList.size() == 0)
      return;

    for (int i = 0; i < itemList.size(); i++) {
      Questionnaire.QuestionnaireItemComponent item = parseItem(itemList.get(i), fhirVersion, baseUrl, containedList, extnesionList);
      itemList.set(i, item);
    }
  }

  private Questionnaire.QuestionnaireItemComponent parseItem(Questionnaire.QuestionnaireItemComponent item, String fhirVersion, String baseUrl,
                                                             Hashtable<String, org.hl7.fhir.r4.model.Resource> containedList, List<Extension> extnesionList) {
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
          return item;

        IBaseResource baseResource = parser.parseResource(stream);

        if (baseResource == null)
          return item;

        Questionnaire subQuestionnaire = (Questionnaire) baseResource;

        // merge extensions
        for (Extension subExtension : subQuestionnaire.getExtension()) {
          if (extnesionList.stream()
              .noneMatch(ext -> ext.getUrl() == subExtension.getUrl() && ext.castToReference(ext.getValue())
                  .getReference() == subExtension.castToReference(subExtension.getValue()).getReference()))
            extnesionList.add(subExtension);
        }



        // merge contained resources
        for (org.hl7.fhir.r4.model.Resource r : subQuestionnaire.getContained()) {
          containedList.put(r.getId(), r);
        }

        return subQuestionnaire.getItem().get(0);
      } catch (IOException ex) {
        // handle if subQuestionniare does not exist
        return item;
      }
    }

    // parser sub-items
    this.parseItemList(item.getItem(), fhirVersion, baseUrl, containedList, extnesionList);

    return item;
  }

  public FileResource getFhirResourceByUrl(String fhirVersion, String resourceType, String url, String baseUrl) {
    logger.info("CdsConnectFileStore::getFhirResourceByUrl(): " + fhirVersion + "/" + resourceType + "/" + url);
    FhirResourceCriteria criteria = new FhirResourceCriteria();
    criteria.setFhirVersion(fhirVersion).setResourceType(resourceType).setUrl(url);
    List<FhirResource> fhirResourceList = fhirResources.findByUrl(criteria);
    return readFhirResourceFromFile(fhirResourceList, fhirVersion, baseUrl);
  }

  // from RuleFinder
  //TODO: these are the same as common, should they be called from there somehow?
  public List<RuleMapping> findRules(CoverageRequirementRuleCriteria criteria) {
    logger.info("CdsConnectFileStore::findRules(): " + criteria.toString());
    return ruleFinder.findRules(criteria);
  }

  public List<RuleMapping> findAll() {
    logger.info("CdsConnectFileStore::findAll()");
    return ruleFinder.findAll();
  }

  //TODO: need to move the VSAC stuff into a common place with CommonFileStore

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
  private ValueSetCache getValueSetCache() {
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
  private void findAndFetchRequiredVSACValueSets(org.hl7.fhir.r4.model.Library library) {
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
  private void findAndFetchRequiredVSACValueSets(org.hl7.fhir.r4.model.Questionnaire questionnaire) {
    findAndFetchRequiredVSACValueSets(questionnaire.getItem());
  }

  /**
   * Looks for ValueSet references in a list of Questionnaire item components in the answerValueSet entries that
   * point to a VSAC ValueSet by OID and have the cache fetch the ValueSet. Also recurses into children item elements.
   *
   * @param itemComponents The FHIR Questionnaire Item components to look for ValueSet references in.
   */
  private void findAndFetchRequiredVSACValueSets(List<org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent> itemComponents) {
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

  protected String findFile(List<CdsConnectFile> files, String name, String extension) {

    String regex = name + "-\\d.\\d.\\d" + extension;
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

  //TODO: duplicated from CommonFileStore, should be put in a shared place
  protected String stripNameFromResourceFilename(String filename, String fhirVersion) {
    // example filename: Library-R4-HomeOxygenTherapy-prepopulation.json
    int fhirIndex = filename.toUpperCase().indexOf(fhirVersion.toUpperCase());
    int startIndex = fhirIndex + fhirVersion.length() + 1;
    int extensionIndex = filename.toUpperCase().indexOf(".json".toUpperCase());
    return filename.substring(startIndex, extensionIndex);
  }

}
