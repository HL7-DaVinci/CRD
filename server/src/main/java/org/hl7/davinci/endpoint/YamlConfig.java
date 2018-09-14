package org.hl7.davinci.endpoint;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class YamlConfig {

  private boolean checkJwt;

  public boolean getCheckJwt() {
    return checkJwt;
  }

  public void setCheckJwt(boolean check) {
    checkJwt = check;
  }

}