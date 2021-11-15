package org.cdshooks;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * The Configuration defined here must match the ConfigurationOptions in
 * CrdExtensionConfigurationOptions.java.
 */
public class Configuration {

  @JsonProperty("alt-drug")
  private Boolean alternativeTherapy = true;

  public Boolean getAlternativeTherapy() { return alternativeTherapy; }

  public void setAlternativeTherapy(Boolean alternativeTherapy) { this.alternativeTherapy = alternativeTherapy; }
}
