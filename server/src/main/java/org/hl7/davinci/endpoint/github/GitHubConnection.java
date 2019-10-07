package org.hl7.davinci.endpoint.github;

import org.hl7.ShortNameMaps;
import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.config.GitHubConfig;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundle;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


import org.kohsuke.github.GitHub;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHContent;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

@Component
@Profile("gitHub")
public class GitHubConnection {

  private static Logger logger = Logger.getLogger(Application.class.getName());

  private String user;
  private String token;
  private String repository;
  private String branch;
  private String artifactPath;
  private String rulePath;

  private GitHub github;
  private GHRepository repo;
  private boolean connected = false;

  @Autowired
  public GitHubConnection(YamlConfig myConfig) {
    GitHubConfig githubConfig = myConfig.getGitHubConfig();
    this.user = githubConfig.getUsername();
    this.token = githubConfig.getToken();
    this.repository = githubConfig.getRepository();
    this.branch = githubConfig.getBranch();
    this.artifactPath = githubConfig.getArtifactPath();
    this.rulePath = githubConfig.getRulePath();

    connect();
  }

  private boolean connect() {
    logger.info("GitHubConnection::connect(): repo: " + repository + ", branch: " + branch);
    // if already connected, don't bother trying to connect again
    if (!connected) {
      try {
        github = GitHub.connect(user, token);
        repo = github.getRepository(repository);
        connected = true;
      } catch (IOException e) {
        logger.warning("GitHubConnection::connect(): ERROR: failed to connect to GitHub! " + e.getMessage());
        connected = false;
      }
    }
    return connected;
  }

  public InputStream getFile(String filename) {
    logger.info("GitHubConnection::getFile(" + filename + ")");
    InputStream fileStream = null;

    // connect if needed
    if (!connect()) {
      return fileStream;
    }

    try {
      GHContent file = repo.getFileContent(artifactPath + "/" + filename, branch);
      fileStream = file.read();

    } catch (IOException e) {
      logger.warning("GitHubConnection::getFile(): ERROR: failed to connect to get file: " + filename + ": " + e.getMessage());
    }
    return fileStream;
  }

  public List<CoverageRequirementRule> getAllRules() {
    logger.info("GitHubConnection::getAllRules():");
    CoverageRequirementRuleCriteria criteria = new CoverageRequirementRuleCriteria();
    return getRules(criteria, false);
  }

  public List<CoverageRequirementRule> getRules(CoverageRequirementRuleCriteria criteria, boolean getFile) {
    logger.info("GitHubConnection::getRules(" + criteria.toString() + ", " + getFile + ")");
    List<CoverageRequirementRule> rules = new ArrayList<>();

    // connect if needed
    if (!connect()) {
      return rules;
    }

    try {
      List<GHContent> payers = repo.getDirectoryContent(rulePath, branch);

      payers.forEach((GHContent payer) -> {
        processPayer(payer, criteria, getFile, rules);
      });

    } catch (Exception e) {
      logger.info("GitHubConnection::getRules(): ERROR: problem getting payers: " + e.getMessage());
    }
    return rules;
  }

  private void processPayer(GHContent payer, CoverageRequirementRuleCriteria criteria, boolean getFile, List<CoverageRequirementRule> rules) {
    if (payer.isDirectory() && (criteria.getPayor() == null || payer.getName().equalsIgnoreCase(criteria.getPayorShortName()))) {
      logger.info("GitHubConnection::processPayer(): " + payer.getName());

      try {
        List<GHContent> codeSystems = repo.getDirectoryContent(payer.getPath(), branch);

        codeSystems.forEach((GHContent codeSystem) -> {
          processCodeSystem(codeSystem, payer.getName(), criteria, getFile, rules);
        });
      } catch (IOException e) {
        logger.warning("GitHubConnection::processPayer(): ERROR: problem getting codesystems: " + e.getMessage());
      }

    } else {
      logger.info("GitHubConnection::processPayer(): skip payer: '" + payer.getName() + "'");
    }
  }

  private void processCodeSystem(GHContent codeSystem, String payerName, CoverageRequirementRuleCriteria criteria, boolean getFile, List<CoverageRequirementRule> rules) {
    if (codeSystem.isDirectory() && (criteria.getCodeSystem() == null || codeSystem.getName().equalsIgnoreCase(criteria.getCodeSystemShortName()))) {
      logger.info("GitHubConnection::processCodeSystem(): " + codeSystem.getName());

      try {
        List<GHContent> codes = repo.getDirectoryContent(codeSystem.getPath(), branch);

        codes.forEach((GHContent code) -> {
          processCode(code, payerName, codeSystem.getName(), criteria, getFile, rules);
        });
      } catch (IOException e) {
        logger.warning("GitHubConnection::processCodeSystem(): ERROR: problem getting codes: " + e.getMessage());
      }
    } else {
      logger.info("GitHubConnection::processCodeSystem(): skip codesystem: '" + codeSystem.getName() + "'");
    }
  }

  private void processCode(GHContent code, String payerName, String codeSystemName, CoverageRequirementRuleCriteria criteria, boolean getFile, List<CoverageRequirementRule> rules) {
    if (code.isDirectory() && (criteria.getCode() == null || code.getName().equalsIgnoreCase(criteria.getCode()))) {
      logger.info("GitHubConnection::processCode(): " + code.getName());

      CoverageRequirementRule rule = new CoverageRequirementRule();

      rule.setId(1);
      rule.setPayor(ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.get(payerName));
      rule.setCodeSystem(ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.get(codeSystemName));
      rule.setCode(code.getName());
      rule.setEditLink(code.getHtmlUrl());

      if (!getFile) {
        rule.setCqlPackagePath("unknown");
        CqlBundle emptyCqlBundle = new CqlBundle();
        emptyCqlBundle.setRawMainCqlLibrary("unknown");
        rule.setCqlBundle(emptyCqlBundle);
      }
      try {
        List<GHContent> files = repo.getDirectoryContent(code.getPath(), branch);

        files.forEach((GHContent file) -> {
          logger.info("GitHubConnection::processCode():     file: " + file.getName());
          if (file.getName().equalsIgnoreCase(code.getName() + ".zip")) {
            try {
              rule.setLink(file.getDownloadUrl());
            } catch (IOException e) {
              logger.warning("GitHubConnection::processCode(): ERROR: failed to get download url");
            }
            if (getFile) {
              //get the file
              try {
                rule.setId(file.hashCode());
                InputStream inputStream = file.read();
                byte[] cqlBundle = IOUtils.toByteArray(inputStream);
                CqlBundle bundle = CqlBundle.fromZip(cqlBundle);
                rule.setCqlBundle(bundle);
              } catch (IOException e) {
                logger.warning("GitHubConnection::processCode(): ERROR: failed to get file contents: " + e.getMessage());
              }
            }
          }
        });
      } catch (IOException e) {
        logger.warning("GitHubConnection::processCode(): ERROR: problem getting files: " + e.getMessage());
      }

      rules.add(rule);

    } else {
      logger.info("GitHubConnection::processCode():skip code: '" + code.getName() + "'");
    }
  }
}
