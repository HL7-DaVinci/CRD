package org.hl7.davinci.endpoint.files.github;

import org.hl7.ShortNameMaps;
import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.config.GitHubConfig;
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
    // if already connected, don't bother trying to connect again
    if (!connected) {
      logger.info("GitHubConnection::connect(): repo: " + repository + ", branch: " + branch);
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

  public List<String> getDirectory(String path) {
    logger.info("GitHubConnection::getDirectory(): " + path);
    ArrayList<String> fileList = new ArrayList<>();

    try {
      List<GHContent> files = repo.getDirectoryContent(path, branch);

      files.forEach((GHContent file) -> {
        fileList.add(file.getName());
      });
    } catch (Exception e) {
      logger.info("GitHubConnection::getDirectory(): ERROR: problem getting directory list: " + e.getMessage());
    }

    return fileList;
  }

  public InputStream getFile(String filePath) {
    logger.info("GitHubConnection::getFile(" + filePath + ")");
    InputStream fileStream = null;

    // connect if needed
    if (!connect()) {
      return fileStream;
    }

    try {
      GHContent file = repo.getFileContent(filePath, branch);
      fileStream = file.read();

    } catch (IOException e) {
      logger.warning("GitHubConnection::getFile(): ERROR: failed to connect to get file: " + filePath + ": " + e.getMessage());
    }
    return fileStream;
  }

  public void downloadRepo() {
    //TODO: perhaps download the entire repo as a zip, extract it and reload from that
    // https://github.com/<repo>/archive/<branch>.zip -- https://github.com/HL7-DaVinci/CDS-Library/archive/master.zip
    // <repo_short>-<branch>.zip -- CDS-Library-master.zip
    String htmlDownload = "https://github.com/" + repository + "/archive/" + branch + ".zip";
    logger.info("GitHubConnection::downloadRepo() htmlDownload: " + htmlDownload);
    String zipFolderName = repo.getName() + "-" + branch;
    String zipFileName = zipFolderName + ".zip";
    logger.info("GitHubConnection::downloadRepo() zipFileName: " + zipFileName);
  }
}
