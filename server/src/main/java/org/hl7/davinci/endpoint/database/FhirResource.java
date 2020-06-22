package org.hl7.davinci.endpoint.database;

import org.apache.commons.lang.StringUtils;

import javax.persistence.*;

@Entity
@IdClass(CompositeFhirResourceTaskId.class)
@Table(name = "fhir_resource")
public class FhirResource {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private String id;

  @Column(name = "url", updatable = false, nullable = true)
  private String url;

  @Id
  @Column(name = "resource_type", nullable = false)
  private String resourceType;

  @Id
  @Column(name = "fhir_version", nullable = false)
  private String fhirVersion;

  @Column(name = "topic", nullable = false)
  private String topic;

  @Column(name = "filename", nullable = false)
  private String filename;

  @Column(name = "path", nullable = true)
  private String path;

  @Column(name = "name", nullable = false)
  private String name;

  private String link = "";

  private String readableTopic = "";

  public String getId() {
    return id;
  }

  public FhirResource setId(String id) {
    this.id = id;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public FhirResource setUrl(String url) {
    this.url = url;
    return this;
  }

  public String getResourceType() {
    return resourceType;
  }

  public FhirResource setResourceType(String resourceType) {
    this.resourceType = resourceType;
    return this;
  }

  public String getFhirVersion() {
    return fhirVersion;
  }

  public FhirResource setFhirVersion(String fhirVersion) {
    this.fhirVersion = fhirVersion;
    return this;
  }

  public String getTopic() {
    return topic;
  }

  public FhirResource setTopic(String topic) {
    this.topic = topic;
    return this;
  }

  public String getFilename() { return filename; }

  public FhirResource setFilename(String filename) {
    this.filename = filename;
    return this;
  }

  public String getPath() { return path; }

  public FhirResource setPath(String path) {
    this.path = path;
    return this;
  }

  public String getName() { return name; }

  public FhirResource setName(String name) {
    this.name = name;
    return this;
  }

  public static String getColumnsString() {
    return "id / resourceType / fhirVersion / topic / filename / name / url";
  }

  public String toString() {
    return id + " / " + resourceType + " / " + fhirVersion + " / " + topic + " / " + filename + " / " + name + " / " + ((url != null) ? url : "null");
  }

  public String getLink() {
    if (link.isEmpty()) {
      return "/fhir/" + fhirVersion + "/" + id;
    } else {
      return link;
    }
  }

  public FhirResource setLink(String link) {
    this.link = link;
    return this;
  }

  public String getReadableTopic() {
    if (readableTopic.isEmpty()) {
      // add a space between the pieces of the CamelCase topic (Camel Case)
      return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(topic), ' ');
    } else {
      return readableTopic;
    }
  }

  public FhirResource setReadableTopic(String readableTopic) {
    this.readableTopic = readableTopic;
    return this;
  }
}
