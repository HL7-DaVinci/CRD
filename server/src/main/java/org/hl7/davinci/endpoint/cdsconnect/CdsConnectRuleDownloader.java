package org.hl7.davinci.endpoint.cdsconnect;

import org.apache.commons.io.FilenameUtils;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundleFile;
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

  public CqlBundleFile getFile(String payer, String codeSystem, String code, String name) {
    String query = payer + "/" + codeSystem + "/" + code;
    logger.info("getFile(" + query + ", " + name + ")");

    CqlBundleFile bundleFile = null;

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
      byte[] cqlBundle = match.getCqlBundle();
      bundleFile = new CqlBundleFile();
      bundleFile.setFilename(name).setResource(new ByteArrayResource(cqlBundle));
    } else {
      logger.info("No matching files found");
    }

    return bundleFile;
  }

  public CqlBundleFile downloadCqlBundleFile(Long id, String name) {
    logger.info("downloadCqlBundleFile(" + id + ", " + name + ")");
    CqlBundleFile bundleFile = null;

    CdsConnectArtifact artifact = new CdsConnectArtifact(connection, connection.retrieveArtifact(id.intValue()));

    List<CdsConnectFile> files = artifact.getFiles();

    if (files.size() == 0) {
      logger.warn("downloadCqlBundleFile(" + id + ", " + name + "): No files found");
      return bundleFile;
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
        logger.warn("downloadCqlBundleFile(" + id + ", " + name + "): file not found");
        return bundleFile;
      }
    }

    bundleFile = new CqlBundleFile();
    bundleFile.setFilename(filename).setResource(new ByteArrayResource(cqlBundle));

    return bundleFile;
  }
}
