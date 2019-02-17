package org.hl7.davinci.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.hl7.davinci.endpoint.config.CdsConnect;
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
  private String launchUrl;
  private boolean checkPractitionerLocation;

  private CdsConnect cdsConnect;

  private String localDbRules;

  public boolean getCheckJwt() {
    return checkJwt;
  }

  public String getLaunchUrl() { return launchUrl; }

  public void setCheckJwt(boolean check) { checkJwt = check; }

  public void setLaunchUrl(String launch) { launchUrl = launch; }

  public void setCheckPractitionerLocation(boolean checkPractitionerLocation) {
    this.checkPractitionerLocation = checkPractitionerLocation;
  }

  public boolean isCheckPractitionerLocation() {
    return checkPractitionerLocation;
  }

  public void setCdsConnect(CdsConnect cdsConnect) { this.cdsConnect = cdsConnect; }

  public String getLocalDbRules() { return localDbRules; }

  public CdsConnect getCdsConnect() { return cdsConnect; }

  public void setLocalDbRules(String localDbRules) { this.localDbRules = localDbRules; }

  public String getContextPath(){
    return env.getProperty("server.servlet.contextPath");
  }
}