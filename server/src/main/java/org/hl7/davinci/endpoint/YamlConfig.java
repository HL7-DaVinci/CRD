package org.hl7.davinci.endpoint;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.hl7.davinci.endpoint.config.CdsConnect;
import org.hl7.davinci.endpoint.config.GitHubConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class YamlConfig {

  // Any property can be read from the .yml resource file
  // in this class.  Ignoring dashes, variables will map to
  // properties of the same name automatically.

  @Autowired
  Environment env;

  private boolean checkJwt;
  private URI launchUrl;
  private boolean includeFilepathInAppContext;
  private boolean checkPractitionerLocation;
  private boolean appendParamsToSmartLaunchUrl;
  private String hostOrg;

  private CdsConnect cdsConnect;

  private GitHubConfig gitHubConfig;

  private String localDbRules;

  private String localDbFhirArtifacts;

  public String getLocalDbFhirArtifacts() {
    return localDbFhirArtifacts;
  }

  public void setLocalDbFhirArtifacts(String localDbFhirArtifacts) {
    this.localDbFhirArtifacts = localDbFhirArtifacts;
  }

  public boolean getCheckJwt() {
    return checkJwt;
  }

  public URI getLaunchUrl() { return launchUrl; }

  public boolean getIncludeFilepathInAppContext() { return includeFilepathInAppContext; }

  public void setCheckJwt(boolean check) { checkJwt = check; }

  public void setLaunchUrl(URI launch) { launchUrl = launch; }

  public YamlConfig setIncludeFilepathInAppContext(boolean includeFilepathInAppContext) {
    this.includeFilepathInAppContext = includeFilepathInAppContext;
    return this;
  }

  public void setCheckPractitionerLocation(boolean checkPractitionerLocation) {
    this.checkPractitionerLocation = checkPractitionerLocation;
  }

  public boolean isCheckPractitionerLocation() {
    return checkPractitionerLocation;
  }

  public String getLocalDbRules() { return localDbRules; }

  public void setLocalDbRules(String localDbRules) { this.localDbRules = localDbRules; }

  public boolean isAppendParamsToSmartLaunchUrl() {
    return appendParamsToSmartLaunchUrl;
  }

  public YamlConfig setAppendParamsToSmartLaunchUrl(boolean appendParamsToSmartLaunchUrl) {
    this.appendParamsToSmartLaunchUrl = appendParamsToSmartLaunchUrl;
    return this;
  }

  public String getContextPath(){
    return env.getProperty("server.servlet.contextPath");
  }

  public String getHostOrg() {return hostOrg;}

  public void setHostOrg(String org) {this.hostOrg = org;}

  public CdsConnect getCdsConnect() { return cdsConnect; }

  public void setCdsConnect(CdsConnect cdsConnect) { this.cdsConnect = cdsConnect; }

  public GitHubConfig getGitHubConfig() { return gitHubConfig; }

  public void setGitHubConfig(GitHubConfig gitHubConfig) { this.gitHubConfig = gitHubConfig; }
}
