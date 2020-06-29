package org.hl7.davinci;

public class FhirResourceInfo {
  private String type = null;
  private String id = null;
  private String name = null;
  private String url = null;

  public String getType() {
    return type;
  }

  public FhirResourceInfo setType(String type) {
    this.type = type;
    return this;
  }

  public String getId() {
    return id;
  }

  public FhirResourceInfo setId(String id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public FhirResourceInfo setName(String name) {
    this.name = name;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public FhirResourceInfo setUrl(String url) {
    this.url = url;
    return this;
  }
}
