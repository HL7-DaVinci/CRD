package org.hl7.davinci.endpoint.database;

import org.apache.commons.io.FilenameUtils;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundleFile;
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

  @Autowired
  DataRepository repository;

  YamlConfig config;

  @Autowired
  public CoverageRequirementRuleDownloaderDatabase(YamlConfig yamlConfig) {
    config = yamlConfig;
  }

  public CqlBundleFile getFile(String payer, String codeSystem, String code, String name) {
    CqlBundleFile bundleFile = null;
    // ignore the payer/codesystem/code
    Path path = Paths.get(config.getLocalDbFhirArtifacts(), name);

    if (FilenameUtils.getExtension(name).toUpperCase().equals("CQL")) {
      logger.info("Converting CQL to ELM");
      try {
        String cql = new String(Files.readAllBytes(path));
        String elm = CqlExecution.translateToElm(cql);
        byte[] elmData = elm.getBytes();

        bundleFile = new CqlBundleFile();
        bundleFile.setFilename(name).setResource(new ByteArrayResource(elmData));

      } catch (Exception e) {
        logger.info("Error: could not convert CQL: " + e.getMessage());
        return bundleFile;
      }
    } else {
      try {
        bundleFile = new CqlBundleFile();
        bundleFile.setResource(new InputStreamResource(new FileInputStream(path.toFile()))).setFilename(name);
      } catch (FileNotFoundException e) {
        logger.info("file not found: " + name);
        bundleFile = null;
      }
    }

    return bundleFile;
  }

  public CqlBundleFile downloadCqlBundleFile(Long id, String name) {
    CqlBundleFile bundleFile = null;

    if (!name.isEmpty()) {
      logger.warn("named file download not supported");
      return bundleFile;
    }

    try {
      Optional<CoverageRequirementRule> rule = repository.findById(id);
      CoverageRequirementRule crr = rule.get();
      String path = crr.getCqlPackagePath();
      String outputName = crr.getCode() + "_" + crr.getId() + ".zip";
      File file = new File(path);
      bundleFile = new CqlBundleFile();
      bundleFile.setResource(new InputStreamResource(new FileInputStream(file))).setFilename(outputName);

    } catch (FileNotFoundException e) {
      logger.info("cql package file not found for id: " + String.valueOf(id));
    }

    return bundleFile;
  }
}
