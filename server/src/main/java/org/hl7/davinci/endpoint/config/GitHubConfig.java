package org.hl7.davinci.endpoint.config;

public class GitHubConfig {
  private String username;
  private String token;
  private String repository;
  private String branch;
  private String artifactPath;
  private String rulePath;

  public String getUsername() {return username; }

  public void setUsername(String username) { this.username = username; }

  public String getToken() { return token; }

  public void setToken(String token) { this.token = token; }

  public String getRepository() { return repository; }

  public void setRepository(String repository) { this.repository = repository; }

  public String getBranch() { return branch; }

  public void setBranch(String branch) { this.branch = branch; }

  public String getArtifactPath() { return artifactPath; }

  public void setArtifactPath(String artifactPath) { this.artifactPath = artifactPath; }

  public String getRulePath() { return rulePath; }

  public void setRulePath(String rulePath) { this.rulePath = rulePath; }
}
