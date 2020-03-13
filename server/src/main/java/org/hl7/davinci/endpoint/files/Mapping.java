package org.hl7.davinci.endpoint.files;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Mapping {
  @JsonProperty("codeSystem")
  private String codeSystem;

  @JsonProperty("codes")
  private String[] codes;

  public String getCodeSystem() { return codeSystem; }
  public void setCodeSystem(String codeSystem) { this.codeSystem = codeSystem; }

  public String[] getCodes() { return codes; }
  public void setCodes(String[] codes) { this.codes = codes; }
}
