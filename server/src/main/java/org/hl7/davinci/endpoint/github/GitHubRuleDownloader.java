package org.hl7.davinci.endpoint.github;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundleFile;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

@Component
@Profile("gitHub")
public class GitHubRuleDownloader implements CoverageRequirementRuleDownloader {

  private static Logger logger = Logger.getLogger(Application.class.getName());

  @Autowired
  private GitHubConnection connection;

  @Autowired
  YamlConfig config;

  public CqlBundleFile getFile(String payer, String codeSystem, String code, String name) {
    //TODO: remove
    // This is a duplicate of the CoverageRequirementRuleDownloaderDatabse.
    // It will be removed when the new file storage location is implemented.

    CqlBundleFile bundleFile = null;
    // ignore the payer/codesystem/code
    Path path = Paths.get(config.getLocalDb().getFhirArtifacts(), name);

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
    logger.info("GitHubRuleDownloader::downloadCqlBundleFile(" + id + ", " + name + ")");
    CqlBundleFile bundleFile = null;
    logger.warning("id named file download not supported");
    return bundleFile;
  }
}
