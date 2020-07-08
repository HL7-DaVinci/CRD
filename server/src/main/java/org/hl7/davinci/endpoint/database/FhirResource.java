package org.hl7.davinci.endpoint.database;

import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.util.UUID;

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

  @Column(name = "topic", nullable = true)
  private String topic;

  @Column(name = "filename", nullable = true)
  private String filename;

  @Column(name = "path", nullable = true)
  private String path;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "data", nullable = true, length = 100000)
  private String data;

  private String link = "";

  private String readableTopic = "";

  public String getId() {
    return id;
  }

  public FhirResource setId(String id) {
    this.id = id;
    return this;
  }

  @PrePersist
  private void ensureId(){
    // set a unique ID if the ID is left blank
    String uuid = UUID.randomUUID().toString();
    if (this.getId() == null) {
      this.setId(uuid);
    }
    // set the name if unset
    if (this.getName() == null) {
      this.setName(uuid);
    }
    // set the filename if unset
    if (this.getFilename() == null) {
      this.setFilename(this.getResourceType() + "-" + this.getFhirVersion() + "-" + this.getName() + ".json");
    }
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

  public String getData() {
    return data;
  }

  public FhirResource setData(String data) {
    this.data = data;
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
      return "/fhir/" + fhirVersion + "/" + resourceType + "/" + id;
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
