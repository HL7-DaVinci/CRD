package org.hl7.davinci.endpoint.database;

public class FhirResourceCriteria {

  private String fhirVersion = null;
  private String resourceType = null;
  private String name = null;
  private String id = null;
  private String url = null;
  private String topic = null;

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

  public String getTopic() { return topic; }

  public FhirResourceCriteria setTopic(String topic) {
    this.topic = topic;
    return this;
  }

  public String toString() {
    String string = new String();
    boolean first = true;
    if (fhirVersion != null) {
      if (first) {
        first = false;
      } else {
        string = string + ", ";
      }
      string = string + "fhirVersion=" + fhirVersion;
    }

    if (resourceType != null) {
      if (first) {
        first = false;
      } else {
        string = string + ", ";
      }
      string = string + "resourceType=" + resourceType;
    }

    if (id != null) {
      if (first) {
        first = false;
      } else {
        string = string + ", ";
      }
      string = string + "id=" + id;
    }

    if (name != null) {
      if (first) {
        first = false;
      } else {
        string = string + ", ";
      }
      string = string + "name=" + name;
    }

    if (url != null) {
      if (first) {
        first = false;
      } else {
        string = string + ", ";
      }
      string = string + "url=" + url;
    }

    if (topic != null) {
      if (first) {
        first = false;
      } else {
        string = string + ", ";
      }
      string = string + "topic=" + topic;
    }
    return string;
  }
}
