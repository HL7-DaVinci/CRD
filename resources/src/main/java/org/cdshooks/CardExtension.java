package org.cdshooks;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class CardExtension {
  
  @JsonProperty("davinci-associated-resource")
  private List<String> associatedResources;

  public CardExtension() {
    associatedResources = new ArrayList<>();
  }

  public CardExtension(List<String> configurationOptions) {
    this.associatedResources = configurationOptions;
  }

  public List<String> getAssociatedResources() {
    return associatedResources;
  }

  public void setAssociatedResources(List<String> associatedResources) {
    this.associatedResources = associatedResources;
  }

  public void addAssociatedResource(String associatedResource) {
    this.associatedResources.add(associatedResource);
  }
}
