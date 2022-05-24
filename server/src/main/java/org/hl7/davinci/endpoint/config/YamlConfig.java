package org.hl7.davinci.endpoint.config;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
  private List<String> corsOrigins;
  private URI launchUrl;
  private boolean checkPractitionerLocation;
  private boolean appendParamsToSmartLaunchUrl;
  private String hostOrg;
  private boolean embedCqlInLibrary;

  private CdsConnect cdsConnect;

  private GitHubConfig gitHubConfig;

  private LocalDb localDb;

  private String valueSetCachePath;

  private boolean urlEncodeAppContext;

  private boolean queryBatchRequest;

  public boolean getCheckJwt() {
    return checkJwt;
  }

  public void setCheckJwt(boolean check) { checkJwt = check; }

  public List<String> getCorsOrigins() { return corsOrigins; }

  public void setCorsOrigins(List<String> origins) { corsOrigins = origins; }

  public URI getLaunchUrl() { return launchUrl; }

  public void setLaunchUrl(URI launch) { launchUrl = launch; }

  public void setCheckPractitionerLocation(boolean checkPractitionerLocation) {
    this.checkPractitionerLocation = checkPractitionerLocation;
  }

  public void setQueryBatchRequest(boolean queryBatchRequest) {
    this.queryBatchRequest = queryBatchRequest;
  }

  public boolean isCheckPractitionerLocation() {
    return checkPractitionerLocation;
  }

  public boolean isQueryBatchRequest() {
    return this.queryBatchRequest;
  }

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

  public boolean getEmbedCqlInLibrary() { return this.embedCqlInLibrary; }

  public void setEmbedCqlInLibrary(boolean embedCqlInLibrary) { this.embedCqlInLibrary = embedCqlInLibrary; }

  public CdsConnect getCdsConnect() { return cdsConnect; }

  public void setCdsConnect(CdsConnect cdsConnect) { this.cdsConnect = cdsConnect; }

  public GitHubConfig getGitHubConfig() { return gitHubConfig; }

  public void setGitHubConfig(GitHubConfig gitHubConfig) { this.gitHubConfig = gitHubConfig; }

  public boolean getUrlEncodeAppContext() { return this.urlEncodeAppContext; }

  public LocalDb getLocalDb() { return localDb; }

  public void setLocalDb(LocalDb localDb) { this.localDb = localDb; }

  public void setUrlEncodeAppContext(boolean urlEncodeAppContext) { this.urlEncodeAppContext = urlEncodeAppContext; }

  public String getValueSetCachePath() { return valueSetCachePath; }

  public void setValueSetCachePath(String valueSetCachePath) { this.valueSetCachePath = valueSetCachePath; }
}
