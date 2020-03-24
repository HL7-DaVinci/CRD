package org.hl7.davinci.endpoint.config;

public class LocalDb {
  private String rules;
  private String fhirArtifacts;
  private String path;

  public String getRules() { return rules; }

  public void setRules(String rules) { this.rules = rules; }

  public String getFhirArtifacts() { return fhirArtifacts; }

  public void setFhirArtifacts(String fhirArtifacts) { this.fhirArtifacts = fhirArtifacts; }

  public String getPath() { return path; }

  public void setPath(String path) { this.path = path; }
}
