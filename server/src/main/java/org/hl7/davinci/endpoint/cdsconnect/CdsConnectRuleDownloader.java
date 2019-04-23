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
import java.util.List;

@Component
@Profile("cdsConnect")
public class CdsConnectRuleDownloader implements CoverageRequirementRuleDownloader {

  static final Logger logger =
      LoggerFactory.getLogger(CdsConnectRuleDownloader.class);

  @Autowired
  private CdsConnectConnection connection;

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
