package org.hl7.davinci.endpoint.config;

public class LocalDb {
  private String path;
  private String examplesPath;

  public String getPath() { return path; }

  public void setPath(String path) { this.path = path; }

  public String getExamplesPath() { return examplesPath; }

  public void setExamplesPath(String examplesPath) { this.examplesPath = examplesPath; }
}
