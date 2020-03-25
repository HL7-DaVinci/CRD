package org.hl7.davinci.endpoint.database;

import java.io.Serializable;
import java.util.Objects;

public class CompositeFhirResourceTaskId implements Serializable {

  private String id;
  private String resourceType;
  private String fhirVersion;

  public CompositeFhirResourceTaskId() { }

  public CompositeFhirResourceTaskId(String id, String resourceType, String fhirVersion) {
    this.id = id;
    this.resourceType = resourceType;
    this.fhirVersion = fhirVersion;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    CompositeFhirResourceTaskId taskId1 = (CompositeFhirResourceTaskId) obj;
    if (!id.equals(taskId1.id)) return false;
    if (!resourceType.equals(taskId1.resourceType)) return false;
    return fhirVersion.equalsIgnoreCase(taskId1.fhirVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, resourceType, fhirVersion);
  }
}
