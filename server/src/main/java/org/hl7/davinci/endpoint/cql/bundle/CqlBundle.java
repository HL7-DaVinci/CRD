package org.hl7.davinci.endpoint.cql.bundle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.elm.r1.VersionedIdentifier;
import org.zeroturnaround.zip.ZipUtil;
import java.io.RandomAccessFile;

public class CqlBundle {
  private JsonNode jsonInfoFile;
  private boolean precompiled;

  private HashMap<VersionedIdentifier, InputStream> rawCqlLibraries = new HashMap<>();
  private VersionedIdentifier mainCqlLibraryId;
  private String rawMainCqlLibrary;

  private void validate() {
    if (precompiled) {
      //todo
    } else {
      if (rawCqlLibraries.size() < 1) {
        throw new RuntimeException("Cql Package missing cql libraries.");
      }
      if (rawCqlLibraries.get(mainCqlLibraryId) == null) {
        throw new RuntimeException("Main Cql Library missing from package.");
      }
    }
  }

  public String getRawMainCqlLibrary() {
    if (rawMainCqlLibrary == null) {
      try {
        rawMainCqlLibrary = IOUtils.toString(rawCqlLibraries.get(mainCqlLibraryId), Charset.defaultCharset());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return rawMainCqlLibrary;
  }

  public boolean isPrecompiled() {
    return precompiled;
  }

  @JsonIgnore
  public RawCqlLibrarySourceProvider getRawCqlLibrarySourceProvider() {
    return new RawCqlLibrarySourceProvider(rawCqlLibraries);
  }

  public static boolean checkIfZip(File file) {
    try {
      RandomAccessFile f = new RandomAccessFile(file, "r");
      long n = f.readInt();
      f.close();
      // check the header to see if it is a zip file
      if (n == 0x504B0304) {
        return true;
      }
    } catch (FileNotFoundException e) {
      // file not found
    } catch (IOException e) {
      // failed to open / close file
    }

    return false;
  }

  public static CqlBundle fromZip(byte[] file) {
    try {
      File temp = File.createTempFile("temp", ".zip");
      FileUtils.writeByteArrayToFile(temp, file);
      return fromZip(temp);
    } catch (IOException e){
      throw new RuntimeException("Error with rule package.");
    }
  }

  public static CqlBundle fromZip(File file) {
    CqlBundle rulePackage = new CqlBundle();
    HashMap<String, byte[]> cqlFiles = new HashMap<>();
    HashMap<String, byte[]> xmlElmFiles = new HashMap<>();

    if (!checkIfZip(file)) {
      throw new RuntimeException("Rule package not a zip file.");
    }

    ZipUtil.iterate(file, (InputStream is, ZipEntry zipEntry) -> {
      String fileName = Paths.get(zipEntry.getName()).getFileName().toString();
      if (fileName.equals("info.json")) {
        rulePackage.jsonInfoFile = new ObjectMapper().readTree(is);
      }
      if (fileName.toLowerCase().endsWith(".cql")) {
        cqlFiles.put(zipEntry.getName(), IOUtils.toByteArray(is));
      }
      if (fileName.toLowerCase().endsWith(".xml")) {
        xmlElmFiles.put(zipEntry.getName(), IOUtils.toByteArray(is));
      }
    });

    if (rulePackage.jsonInfoFile == null) {
      throw new RuntimeException("Package is missing an info.json file.");
    }

    List<CqlLibrary> cqlLibraries = new ArrayList<>();
    for (String fileName: cqlFiles.keySet()){
      CqlLibrary cqlLibrary = new CqlLibrary();
      cqlLibrary.cql = cqlFiles.get(fileName);
      String elmName = fileName.substring(0,fileName.length()-4) + ".xml";
      if (xmlElmFiles.containsKey(elmName)) {
        cqlLibrary.xmlElm = xmlElmFiles.get(elmName);
      }
      cqlLibraries.add(cqlLibrary);
    }

    // todo: consider deserializing into a class, ie with jackson
    rulePackage.precompiled = !rulePackage.jsonInfoFile.get("compileCql").asBoolean();
    String mainCqlLibraryName = rulePackage.jsonInfoFile.get("mainCqlLibraryName").asText();

    for (CqlLibrary cqlLibrary: cqlLibraries){
      if (rulePackage.isPrecompiled()) {
        if (cqlLibrary.xmlElm == null) {
          throw new RuntimeException("Package indicated CQL was precompiled, but elm xml missing.");
        }
        // TODO: need to set rulePackage.xmlElmLibraries and mainCqlLibraryId
      } else {
        InputStream cqlStream = new ByteArrayInputStream(cqlLibrary.cql);
        VersionedIdentifier id = getIdFromCqlFile(cqlLibrary.cql);
        rulePackage.rawCqlLibraries.put(getIdFromCqlFile(cqlLibrary.cql),cqlStream);
        if (id.getId().equals(mainCqlLibraryName)) {
          rulePackage.mainCqlLibraryId = id;
        }
      }
    }

    rulePackage.validate();
    return rulePackage;
  }

  @JsonIgnore
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
    public byte[] xmlElm;
  }
}
