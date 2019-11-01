package org.hl7.davinci.endpoint.github;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundleFile;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.io.InputStream;

import java.util.logging.Logger;

@Component
@Profile("gitHub")
public class GitHubRuleDownloader implements CoverageRequirementRuleDownloader {

  private static Logger logger = Logger.getLogger(Application.class.getName());

  @Autowired
  private GitHubConnection connection;

  public CqlBundleFile getFile(String payer, String codeSystem, String code, String name) {
    CqlBundleFile bundleFile = null;
    String query = payer + "/" + codeSystem + "/" + code;
    logger.info("GitHubRuleDownloader::getFile(" + query + ", " + name + ")");
    InputStream fileStream = connection.getFile(name);

    if (FilenameUtils.getExtension(name).toUpperCase().equals("CQL")) {
      logger.info("Converting CQL to ELM");
      try {
        String fileContent = IOUtils.toString(fileStream, StandardCharsets.UTF_8.name());
        String elm = CqlExecution.translateToElm(fileContent);
        byte[] elmData = elm.getBytes();

        bundleFile = new CqlBundleFile();
        bundleFile.setFilename(name).setResource(new ByteArrayResource(elmData));

      } catch (Exception e) {
        logger.info("Error: could not convert CQL: " + e.getMessage());
        return bundleFile;
      }
    } else {
      bundleFile = new CqlBundleFile();
      bundleFile.setFilename(name).setResource(new InputStreamResource(fileStream));
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
