package org.hl7.davinci.endpoint.cdsconnect;

import org.apache.commons.io.FilenameUtils;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.files.FileResource;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("cdsConnect")
public class CdsConnectRuleDownloader implements CoverageRequirementRuleDownloader {

  static final Logger logger =
      LoggerFactory.getLogger(CdsConnectRuleDownloader.class);

  @Autowired
  private CdsConnectConnection connection;

  public FileResource getFile(String payer, String codeSystem, String code, String name) {
    String query = payer + "/" + codeSystem + "/" + code;
    logger.info("getFile(" + query + ", " + name + ")");

    FileResource fileResource = null;

    CdsConnectRuleList ruleList = connection.queryForRulesList(query);

    List<CdsConnectArtifact> artifacts = ruleList.getArtifacts();

    List<CdsConnectFile> allFiles = new ArrayList<>();

    // add all of the files from the artifacts found to a single list
    for (CdsConnectArtifact artifact : artifacts) {
      allFiles.addAll(artifact.getFiles());
    }

    CdsConnectFile match = null;

    // loop through the files from the matching rules until we find the one we are looking for
    for (CdsConnectFile file : allFiles) {
      String filename = file.getFilename();

      // if we find an exact match then we are done
      if (FilenameUtils.getName(name).equals(FilenameUtils.getName(filename))) {
        logger.info("getFile: exact match found");
        match = file;
        break;
      }

      // If no exact match is found, we want to find a close match.
      // The Drupal file upload adds a "_#" to the end of duplicate named files uploaded.
      // This strips that off to find a file that is named properly enough to match.
      String n = FilenameUtils.getName(filename);
      Integer lastIndex = n.lastIndexOf("_");
      if (lastIndex > 0) {
        String part = n.substring(0, lastIndex);
        if (FilenameUtils.getName(name).equals(part + "." + FilenameUtils.getExtension(filename))) {
          logger.info("getFile: close match found: " + filename);
          match = file;
        }
      }
    }

    if (match != null) {
      if (FilenameUtils.getExtension(name).toUpperCase().equals("CQL")) {
        logger.info("Converting CQL to ELM");
        try {
          String elm = CqlExecution.translateToElm(new String(match.getCqlBundle()));
          byte[] elmData = elm.getBytes();

          fileResource = new FileResource();
          fileResource.setFilename(name).setResource(new ByteArrayResource(elmData));

        } catch (Exception e) {
          logger.info("Error: could not convert CQL: " + e.getMessage());
          return fileResource;
        }

      } else {
        byte[] fileData = match.getCqlBundle();
        fileResource = new FileResource();
        fileResource.setFilename(name).setResource(new ByteArrayResource(fileData));
      }
    } else {
      logger.info("No matching files found");
    }

    return fileResource;
  }

  /*
  public FileResource downloadCqlFile(Long id, String name) {
    logger.info("downloadCqlFile(" + id + ", " + name + ")");
    FileResource fileResource = null;

    CdsConnectArtifact artifact = new CdsConnectArtifact(connection, connection.retrieveArtifact(id.intValue()));

    List<CdsConnectFile> files = artifact.getFiles();

    if (files.size() == 0) {
      logger.warn("downloadCqlFile(" + id + ", " + name + "): No files found");
      return fileResource;
    }

    byte[] cqlBundle = null;
    String filename = "";

    if (name.isEmpty()) {
      // grab the first one
      logger.info("downloading the first file");
      cqlBundle = files.get(0).getCqlBundle();
      String extension = FilenameUtils.getExtension(files.get(0).getFilename());

      try {

        ByteBuffer cqlBundleWrapped = ByteBuffer.wrap(cqlBundle, 0, 4);
        int firstInt = cqlBundleWrapped.getInt();
        if (firstInt == 0x504B0304) {
          // if file is zip, force extension
          extension = "zip";
        }
        filename = artifact.getCode() + "_" + artifact.getId() + "." + extension;

      } catch (RuntimeException e) {
        logger.info("Error: could not process cql package: " + e.getMessage());
      }
    } else {
      // loop through the files trying to match the name
      for (CdsConnectFile file : files) {
        if (name.equals(FilenameUtils.getName(file.getFilename()))) {
          cqlBundle = file.getCqlBundle();
          filename = name;
          break;
        }
      }

      if (cqlBundle == null) {
        // file not found
        logger.warn("downloadCqlile(" + id + ", " + name + "): file not found");
        return fileResource;
      }
    }

    fileResource = new FileResource();
    fileResource.setFilename(filename).setResource(new ByteArrayResource(cqlBundle));

    return fileResource;
  }
  */
}
