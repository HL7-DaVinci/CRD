package org.hl7.davinci.ehrserver.authproxy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfig {

  @Bean
  public PayloadDAOImpl payloadDAO() {
    PayloadDAOImpl pdi = new PayloadDAOImpl();
    return pdi;
  }
}
