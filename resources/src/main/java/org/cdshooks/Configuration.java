package org.cdshooks;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * The Configuration defined here must match the ConfigurationOptions in
 * CrdExtensionConfigurationOptions.java.
 */
public class Configuration {

  @JsonProperty("alt-drug")
  private Boolean alternativeTherapy = true;
  @JsonProperty("dtr-clin")
  private Boolean dtrClin = true;
  @JsonProperty("prior-auth")
  private Boolean priorAuth = true;
  @JsonProperty("coverage")
  private Boolean coverage = true;

  @JsonProperty("max-cards")
  private int maxCards = 10;

  public Boolean getAlternativeTherapy() { return alternativeTherapy; }

  public void setAlternativeTherapy(Boolean alternativeTherapy) { this.alternativeTherapy = alternativeTherapy; }

  public Boolean getDTRClin() { return dtrClin; }
  public Boolean getPriorAuth() { return priorAuth; }
  public Boolean getCoverage() { return coverage; }
  public int getMaxCards() { return maxCards; }

  public boolean canAddCard(int currentCardCount) {
    return currentCardCount <= maxCards;
  }
}
