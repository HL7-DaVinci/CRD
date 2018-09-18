package org.cdshooks;

public class Action {

  private TypeEnum type = null;
  private String description = null;
  private Object resource = null;

  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Object getResource() {
    return resource;
  }

  public void setResource(Object resource) {
    this.resource = resource;
  }

  public enum TypeEnum {
    create,
    update,
    delete
  }
}
