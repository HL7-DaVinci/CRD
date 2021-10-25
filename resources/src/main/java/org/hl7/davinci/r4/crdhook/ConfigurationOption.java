package org.hl7.davinci.r4.crdhook;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigurationOption {

  private String code;
  private String type;
  private String name;
  private String description;
  @JsonProperty("value")
  private Boolean defaultValue;

  public ConfigurationOption(String code, String type, String name, String description, Boolean defaultValue) {
    this.code = code;
    this.type = type;
    this.name = name;
    this.description = description;
    this.defaultValue = defaultValue;
  }

  public String getCode() { return code; }

  public void setCode(String code) { this.code = code; }

  public String getType() { return type; }

  public void setType(String type) { this.type = type; }

  public String getName() { return name; }

  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }

  public void setDescription(String description) { this.description = description; }

  public Boolean getDefaultValue() { return defaultValue; }

  public void setDefaultValue(Boolean defaultValue) { this.defaultValue = defaultValue; }
}
