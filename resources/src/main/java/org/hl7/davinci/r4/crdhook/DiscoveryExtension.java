package org.hl7.davinci.r4.crdhook;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DiscoveryExtension {

  @JsonProperty("davinci-crd.configuration-options")
  private List<ConfigurationOption> configurationOptions;

  public DiscoveryExtension(List<ConfigurationOption> configurationOptions) {
    this.configurationOptions = configurationOptions;
  }

  public List<ConfigurationOption> getConfigurationOptions() {
    return configurationOptions;
  }

  public void setConfigurationOptions(List<ConfigurationOption> configurationOptions) {
    this.configurationOptions = configurationOptions;
  }

  public void addConfigurationOption(ConfigurationOption configurationOption) {
    this.configurationOptions.add(configurationOption);
  }
}
