package org.hl7.davinci.endpoint.files;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TopicMetadata {
  @JsonProperty("topic")
  private String topic;

  @JsonProperty("payers")
  private String[] payers;

  @JsonProperty("fhirVersions")
  private String[] fhirVersions;

  @JsonProperty("mappings")
  private Mapping[] mappings;

  public String getTopic() { return topic; }
  public void setTopic(String topic) { this.topic = topic; }

  public String[] getPayers() { return payers; }
  public void setPayers(String[] payers) { this.payers = payers; }

  public String[] getFhirVersions() { return fhirVersions; }
  public void setFhirVersions(String[] fhirVersions) { this.fhirVersions = fhirVersions; }

  public Mapping[] getMappings() { return mappings; }
  public void setMappings(Mapping[] mappings) { this.mappings = mappings; }
}
