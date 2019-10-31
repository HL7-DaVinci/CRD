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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHContent;

@Component
@Profile("gitHub")
public class GitHubConnection {

  private static Logger logger = Logger.getLogger(Application.class.getName());

  private String user;
  private String token;
  private String repository;
  private String artifactPath;
  private String rulePath;

  @Autowired
  public GitHubConnection(YamlConfig myConfig) {
    GitHubConfig githubConfig = myConfig.getGitHubConfig();
    this.user = githubConfig.getUsername();
    this.token = githubConfig.getToken();
    this.repository = githubConfig.getRepository();
    this.artifactPath = githubConfig.getArtifactPath();
    this.rulePath = githubConfig.getRulePath();
  }

  public InputStream getFile(String filename) {
    logger.info("GitHubConnection::getFile(" + filename + ")");
    InputStream fileStream = null;
    try {
      GitHub github = GitHub.connect(user, token);
      GHRepository repo = github.getRepository(repository);
      GHContent file = repo.getFileContent(artifactPath + "/" + filename);
      fileStream = file.read();

    } catch (IOException e) {
      e.printStackTrace();
    }
    return fileStream;
  }

  public List<CoverageRequirementRule> getAllRules() {
    logger.info("GitHubConnection::getAllRules():");
    CoverageRequirementRuleCriteria criteria = new CoverageRequirementRuleCriteria();
    return getRules(criteria, false);
  }

  public void processPayer(CoverageRequirementRuleCriteria criteria, boolean getfile, List<CoverageRequirementRule> rules) {
  }

  public void processCodeSystem(CoverageRequirementRuleCriteria criteria, boolean getfile, List<CoverageRequirementRule> rules) {
  }

  public void processCode(CoverageRequirementRuleCriteria criteria, boolean getfile, List<CoverageRequirementRule> rules) {

  }

  public List<CoverageRequirementRule> getRules(CoverageRequirementRuleCriteria criteria, boolean getFile) {
    logger.info("GitHubConnection::getRules(" + criteria.toString() + ", " + getFile + ")");
    List<CoverageRequirementRule> rules = new ArrayList<>();
    try {
      GitHub github = GitHub.connect(user, token);
      GHRepository repo = github.getRepository(repository);
      List<GHContent> payers = repo.getDirectoryContent(rulePath);

      payers.forEach((GHContent payer)->{

        if (payer.isDirectory() && (criteria.getPayor() == null || payer.getName().equalsIgnoreCase(criteria.getPayorShortName()))) {
          logger.info("payer: " + payer.getName());

          try {
            List<GHContent> codeSystems = repo.getDirectoryContent(payer.getPath());

            codeSystems.forEach((GHContent codeSystem)->{
              if (codeSystem.isDirectory() && (criteria.getCodeSystem() == null || codeSystem.getName().equalsIgnoreCase(criteria.getCodeSystemShortName()))) {
                logger.info("    codeSystem: " + codeSystem.getName());

                try {
                  List<GHContent> codes = repo.getDirectoryContent(codeSystem.getPath());

                  codes.forEach((GHContent code)->{
                    if (code.isDirectory() && (criteria.getCode() == null|| code.getName().equalsIgnoreCase(criteria.getCode()))) {
                      logger.info("        code: " + code.getName());

                      CoverageRequirementRule rule = new CoverageRequirementRule();

                      rule.setId(1);
                      rule.setPayor(ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.get(payer.getName()));
                      rule.setCodeSystem(ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.get(codeSystem.getName()));
                      rule.setCode(code.getName());
                      rule.setEditLink(code.getHtmlUrl());

                      if (!getFile) {
                        rule.setCqlPackagePath("unknown");
                        CqlBundle emptyCqlBundle = new CqlBundle();
                        emptyCqlBundle.setRawMainCqlLibrary("unknown");
                        rule.setCqlBundle(emptyCqlBundle);
                      }
                      try {
                        List<GHContent> files = repo.getDirectoryContent(code.getPath());

                        files.forEach((GHContent file)->{
                          logger.info("            file: " + file.getName());
                          if (file.getName().equalsIgnoreCase(code.getName() + ".zip")) {
                            try {
                              rule.setLink(file.getDownloadUrl());
                            } catch (IOException e) {
                              logger.info("ERROR: failed to get download url");
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
                                logger.info("ERROR: failed to get file contents: " + e.getMessage());
                              }
                            }
                          }
                        });
                      } catch (IOException e) {
                        logger.info("ERROR: problem getting files: " + e.getMessage());
                      }

                      rules.add(rule);

                    } else {
                      logger.info("skip code: '" + code.getName() + "'");
                    }
                  });
                } catch (IOException e) {
                  logger.info("ERROR: problem getting codes: " + e.getMessage());
                }
              } else {
                logger.info("skip codesystem: '" + codeSystem.getName() + "'");
              }
            });
          } catch (IOException e) {
            logger.info("ERROR: problem getting codesystems: " + e.getMessage());
          }

        } else {
          logger.info("skip payer: '" + payer.getName() + "'");
        }
      });

    } catch (Exception e) {
      logger.info("ERROR: problem getting payers: " + e.getMessage());
    }
    return rules;
  }
}
