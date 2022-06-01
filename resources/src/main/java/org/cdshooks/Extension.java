package org.cdshooks;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Extension {

  @JsonProperty("davinci-crd.configuration")
  private Configuration configuration;

  public Configuration getConfiguration() { return configuration; }

  public void setConfiguration(Configuration configuration) { this.configuration = configuration; }

  public String toString(){
    return "Extension configuration: " + configuration;
  }
}
