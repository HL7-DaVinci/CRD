package org.cdshooks;

public class AlternativeTherapy {
  private boolean applies;
  private String code;
  private String system;
  private String display;

  public boolean getApplies() {
    return applies;
  }

  public AlternativeTherapy setApplies(boolean applies) {
    this.applies = applies;
    return this;
  }

  public String getCode() {
    return code;
  }

  public AlternativeTherapy setCode(String code) {
    this.code = code;
    return this;
  }

  public String getSystem() {
    return system;
  }

  public AlternativeTherapy setSystem(String system) {
    this.system = system;
    return this;
  }

  public String getDisplay() {
    return display;
  }

  public AlternativeTherapy setDisplay(String display) {
    this.display = display;
    return this;
  }

  public String toString() {
    return display + " (" + code + " - " + system + ")";
  }
}
