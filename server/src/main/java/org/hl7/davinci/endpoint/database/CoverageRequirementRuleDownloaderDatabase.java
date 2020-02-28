package org.hl7.davinci.endpoint.database;

import org.apache.commons.io.FilenameUtils;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.files.FileResource;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

@Component
@Profile("localDb")
public class CoverageRequirementRuleDownloaderDatabase implements CoverageRequirementRuleDownloader {

  static final Logger logger =
      LoggerFactory.getLogger(CoverageRequirementRuleDownloaderDatabase.class);

  YamlConfig config;

  @Autowired
  public CoverageRequirementRuleDownloaderDatabase(YamlConfig yamlConfig) {
    config = yamlConfig;
  }

  public FileResource getFile(String payer, String codeSystem, String code, String name) {
    FileResource fileResource = null;
    // ignore the payer/codesystem/code
    Path path = Paths.get(config.getLocalDb().getFhirArtifacts(), name);

    if (FilenameUtils.getExtension(name).toUpperCase().equals("CQL")) {
      logger.info("Converting CQL to ELM");
      try {
        String cql = new String(Files.readAllBytes(path));
        String elm = CqlExecution.translateToElm(cql);
        byte[] elmData = elm.getBytes();

        fileResource = new FileResource();
        fileResource.setFilename(name).setResource(new ByteArrayResource(elmData));

      } catch (Exception e) {
        logger.info("Error: could not convert CQL: " + e.getMessage());
        return fileResource;
      }
    } else {
      try {
        fileResource = new FileResource();
        fileResource.setResource(new InputStreamResource(new FileInputStream(path.toFile()))).setFilename(name);
      } catch (FileNotFoundException e) {
        logger.info("file not found: " + name);
        fileResource = null;
      }
    }

    return fileResource;
  }
}
