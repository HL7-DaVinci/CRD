package org.hl7.davinci.endpoint.database;

public class FhirResourceCriteria {

  private String fhirVersion;
  private String resourceType;
  private String name;
  private String id;
  private String url;

  public String getFhirVersion() { return fhirVersion; }

  public FhirResourceCriteria setFhirVersion(String fhirVersion) {
    this.fhirVersion = fhirVersion;
    return this;
  }

  public String getResourceType() { return resourceType; }

  public FhirResourceCriteria setResourceType(String resourceType) {
    this.resourceType = resourceType;
    return this;
  }

  public String getName() { return name; }

  public FhirResourceCriteria setName(String name) {
    this.name = name;
    return this;
  }

  public String getId() { return id; }

  public FhirResourceCriteria setId(String id) {
    this.id = id;
    return this;
  }

  public String getUrl() { return url; }

  public FhirResourceCriteria setUrl(String url) {
    this.url = url;
    return this;
  }

  public String toString() {
    return String.format(
        "fhirVersion=%s, resourceType=%s, name=%s", fhirVersion, resourceType, name
    );
  }
}
