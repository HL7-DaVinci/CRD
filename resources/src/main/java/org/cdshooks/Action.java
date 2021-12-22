package org.cdshooks;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.fhir.instance.model.api.IBaseResource;


@JsonSerialize(using = ActionSerializer.class)
public class Action {

  public Action(FhirComponentsT fhirComponents) {
    this.fhirComponents = fhirComponents;
  }

  private TypeEnum type = null;
  private String description = null;
  private IBaseResource resource = null;
  private String resourceId = null;

  private FhirComponentsT fhirComponents;

  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public String getDescription() { return description; }

  public void setDescription(String description) {
    this.description = description;
  }

  public IBaseResource getResource() { return resource; }

  public void setResource(IBaseResource resource) {
    this.resource = resource;
    setResourceId(resource.fhirType() + "/" + resource.getIdElement().getIdPart());
  }

  public String getResourceId() { return resourceId; }

  public void setResourceId(String resourceId) { this.resourceId = resourceId; }

  public FhirComponentsT getFhirComponents() {
    return this.fhirComponents;
  }

  public enum TypeEnum {
    create,
    update,
    delete
  }


}
