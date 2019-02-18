package org.hl7.davinci.endpoint;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class YamlConfig {

  // Any property can be read from the .yml resource file
  // in this class.  Ignoring dashes, variables will map to
  // properties of the same name automatically.

  private boolean checkJwt;
  private String launchUrl;
  private boolean checkPractitionerLocation;

  private boolean useCdsConnect;
  private String cdsConnectUrl;
  private String cdsConnectUsername;
  private String cdsConnectPassword;

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

  public boolean getUseCdsConnect() { return useCdsConnect; }

  public String getCdsConnectUrl() { return cdsConnectUrl; }

  public String getCdsConnectUsername() { return cdsConnectUsername; }

  public String getCdsConnectPassword() { return cdsConnectPassword; }

  public void setUseCdsConnect(boolean use) { useCdsConnect = use; }

  public void setCdsConnectUrl(String cdsConnect) { cdsConnectUrl = cdsConnect; }

  public void setCdsConnectUsername(String cdsConnectUsername) { cdsConnectUsername = cdsConnectUsername; }

  public void setCdsConnectPassword(String cdsConnectPassword) { cdsConnectPassword = cdsConnectPassword; }
}