package org.hl7.davinci.endpoint.cql;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.elm.r1.UsingDef;
import org.hl7.elm.r1.VersionedIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CqlRule {

  static final Logger logger = LoggerFactory.getLogger(CqlRule.class);

  private String fhirVersion;
  private String mainCqlLibraryName = "Rule";

  private boolean precompiled = false;

  private HashMap<String, HashMap<VersionedIdentifier, InputStream>> rawCqlLibraries = new HashMap<>();
  private HashMap<String, VersionedIdentifier> mainCqlLibraryId = new HashMap<>();


  /**
   * Construct an empty CqlRule, this may be removed in the future.
   */
  public CqlRule() { }

  /**
   * Build a CqlRule from a lis tof CQL Files.
   * @param mainCqlLibraryName
   * @param cqlFiles
   * @param fhirVersion
   */
  public CqlRule(String mainCqlLibraryName, HashMap<String, byte[]> cqlFiles, String fhirVersion) {
    logger.info("CqlRule::constructor() cqlFiles: fhirVersion: " + fhirVersion);
    this.mainCqlLibraryName = mainCqlLibraryName;
    this.fhirVersion = fhirVersion;

    build(cqlFiles, new HashMap<>(), new HashMap<>());
  }

  /**
   * Old method of building a CQL rule from the old file structure.
   * @param cqlRulePath
   * @param fhirVersion
   */
  public CqlRule(String cqlRulePath, String fhirVersion) {
    logger.info("CqlRule::constructor() rulePath: " + cqlRulePath + ", fhirVersion: " + fhirVersion);
    this.fhirVersion = fhirVersion;

    HashMap<String, byte[]> cqlFiles = new HashMap<>();
    HashMap<String, byte[]> jsonElmFiles = new HashMap<>();
    HashMap<String, byte[]> xmlElmFiles = new HashMap<>();

    // process all fo the files in the directory finding the CQL, ELM (JSON), ELM (XML)
    File folder = new File(cqlRulePath);
    for (File file: folder.listFiles()) {
      if (file.isFile()) {
        try {
          if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("cql")) {
            cqlFiles.put(file.getName(), Files.readAllBytes(file.toPath()));
          } else if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("json")) {
            jsonElmFiles.put(file.getName(), Files.readAllBytes(file.toPath()));
          } else if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("xml")) {
            xmlElmFiles.put(file.getName(), Files.readAllBytes(file.toPath()));
          }
        } catch (IOException e) {
          logger.warn("failed to open file: " + file.getAbsoluteFile());
        }
      }
    }

    build(cqlFiles, jsonElmFiles, xmlElmFiles);
  }

  private void build(HashMap<String, byte[]> cqlFiles,
                     HashMap<String, byte[]> jsonElmFiles,
                     HashMap<String, byte[]> xmlElmFiles) {

    // build a list of all of the CQL Libraries
    List<CqlRule.CqlLibrary> cqlLibraries = new ArrayList<>();
    for (String fileName : cqlFiles.keySet()) {
      logger.debug("CqlRule: file: " + fileName);
      CqlRule.CqlLibrary cqlLibrary = new CqlRule.CqlLibrary();
      cqlLibrary.cql = cqlFiles.get(fileName);

      // only add those that are the right fhir version
      String fhirVersionFromCql = getFhirVersionFromCqlFile(cqlLibrary.cql);

      // last character of fhirVersion ("R4" => "4") and first character of FHIR version from file ("3.0.0" => "3")
      if (fhirVersion.substring(fhirVersion.length()-1).equalsIgnoreCase(fhirVersionFromCql.substring(0, 1))) {

        String fileNameWithoutExtension = fileName.substring(0, fileName.length() - 4);
        String xmlElmName = fileNameWithoutExtension + ".xml";
        if (xmlElmFiles.containsKey(xmlElmName)) {
          cqlLibrary.xlmElm = true;
          cqlLibrary.elm = xmlElmFiles.get(xmlElmName);
        }
        String jsonElmName = fileNameWithoutExtension + ".json";
        if (jsonElmFiles.containsKey(jsonElmName)) {
          cqlLibrary.xlmElm = false;
          cqlLibrary.elm = jsonElmFiles.get(jsonElmName);
        }

        cqlLibraries.add(cqlLibrary);
      }
    }

    precompiled = false;

    for (CqlLibrary cqlLibrary : cqlLibraries) {
      if (precompiled) {
        if (cqlLibrary.elm == null) {
          throw new RuntimeException("Package indicated CQL was precompiled, but elm xml missing.");
        }
        // TODO: need to set rulePackage.elmLibraries and mainCqlLibraryId
      } else {
        InputStream cqlStream = new ByteArrayInputStream(cqlLibrary.cql);
        VersionedIdentifier id = getIdFromCqlFile(cqlLibrary.cql);
        String fhirVersionFromFile = getFhirVersionFromCqlFile(cqlLibrary.cql);
        logger.info("CqlRule::Constructor() add id: " + id.getId() + ", fhir version: " + fhirVersionFromFile);

        if (rawCqlLibraries.containsKey(fhirVersionFromFile)) {
          //logger.info("CqlRule::Constructor() add rawCqlLibraries add: " + id.getId());
          rawCqlLibraries.get(fhirVersionFromFile).put(id, cqlStream);
        } else {
          HashMap<VersionedIdentifier, InputStream> map = new HashMap<>();
          map.put(id, cqlStream);
          //logger.info("CqlRule::Constructor() add rawCqlLibraries new: " + id.getId());
          rawCqlLibraries.put(fhirVersionFromFile, map);
        }
        if (id.getId().equals(mainCqlLibraryName)) {
          //logger.info("CqlRule::Constructor() add mainCqlLibraryId: " + id.getId());
          mainCqlLibraryId.put(fhirVersionFromFile, id);
        }
      }
    }
  }

  public boolean isPrecompiled() {
    return precompiled;
  }

  public RawCqlLibrarySourceProvider getRawCqlLibrarySourceProvider(String fhirVersion) {
    logger.info("CqlRule::getRawCqlLibrarySourceProvider(): " + fhirVersion);
    return new RawCqlLibrarySourceProvider(rawCqlLibraries.get(fhirVersion));
  }

  public String getRawMainCqlLibrary(String fhirVersion) {
    logger.info("CqlRule::getRawMainCqlLibrary(): " + fhirVersion);

    try {
      if (!mainCqlLibraryId.containsKey(fhirVersion) || !rawCqlLibraries.containsKey(fhirVersion)) {
        return null;
      }
      String ret = IOUtils.toString(rawCqlLibraries.get(fhirVersion).get(mainCqlLibraryId.get(fhirVersion)), Charset.defaultCharset());
      return ret;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String getFhirVersionFromCqlFile(byte[] cql) {
    String fhirVersion = "";
    UsingDef usingDef = new UsingDef();
    Pattern pattern = Pattern.compile("using (.*?) version '(.*?)'");
    try {
      Matcher matcher = pattern.matcher(new String(cql));
      while (fhirVersion.isEmpty()) {
        matcher.find();
        if (matcher.groupCount() != 2) {
          throw new RuntimeException("Encountered bad CQL file, could not detect library identifier.");
        }
        if (matcher.group(1).equalsIgnoreCase("FHIR")) {
          fhirVersion = matcher.group(2);
        }
      }
    } catch (Exception e) {
      logger.warn("exception in CqlRule::getFhirVersionFromCqlFile(): " + e.getMessage());
    }
    return fhirVersion;
  }

  private static VersionedIdentifier getIdFromCqlFile(byte[] cql){
    VersionedIdentifier libraryIdentifier = new VersionedIdentifier();
    Pattern pattern = Pattern.compile("library (.*?) version '(.*?)'");
    Matcher matcher = pattern.matcher(new String(cql));
    matcher.find();
    if (matcher.groupCount() != 2) {
      throw new RuntimeException("Encountered bad CQL file, could not detect library identifier.");
    }
    libraryIdentifier.setId(matcher.group(1));
    libraryIdentifier.setVersion(matcher.group(2));
    return libraryIdentifier;
  }

  static class CqlLibrary {
    public byte[] cql;
    /// if true, elm is in XML format. If false, ELM is in JSON format.
    public boolean xlmElm;
    public byte[] elm;
  }
}
