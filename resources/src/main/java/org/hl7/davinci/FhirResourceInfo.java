package org.hl7.davinci;

/**
 * The FhirResourceInfo class contains common information found within FHIR resources.
 * It is used to make passing the information together easier. A FHIR resource processor
 * can use this to pass the information back from multiple different resource types.
 */
public class FhirResourceInfo {
  private String type = null;
  private String id = null;
  private String name = null;
  private String url = null;

  boolean valid = false;

  public Boolean valid() {
    // setting any of the values other than type (even to null) makes the class valid
    return valid;
  }

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
    valid = true;
    return this;
  }

  public String getName() {
    return name;
  }

  public FhirResourceInfo setName(String name) {
    this.name = name;
    valid = true;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public FhirResourceInfo setUrl(String url) {
    this.url = url;
    valid = true;
    return this;
  }
}
