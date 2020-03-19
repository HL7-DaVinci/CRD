package org.hl7.davinci.endpoint.database;

import javax.persistence.*;

@Entity
@IdClass(CompositeFhirResourceTaskId.class)
@Table(name = "fhir_resource")
public class FhirResource {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private String id;

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

  @Column(name = "name", nullable = false)
  private String name;

  public String getId() {
    return id;
  }

  public FhirResource setId(String id) {
    this.id = id;
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

  public String getName() { return name; }

  public FhirResource setName(String name) {
    this.name = name;
    return this;
  }

  public static String getColumnsString() {
    return "id / resourceType / fhirVersin / topic / filename / name";
  }

  public String toString() {
    return id + " / " + resourceType + " / " + fhirVersion + " / " + topic + " / " + filename + " / " + name;
  }
}
