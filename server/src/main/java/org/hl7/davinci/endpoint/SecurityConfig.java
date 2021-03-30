package org.hl7.davinci.endpoint;

import com.google.common.collect.ImmutableList;
import org.hl7.davinci.endpoint.config.YamlConfig;
import org.hl7.davinci.endpoint.database.PublicKeyRepository;
import org.hl7.davinci.endpoint.database.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private YamlConfig myConfig;

  @Autowired
  private RequestService requestService;

  @Autowired
  private PublicKeyRepository publicKeyRepository;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors();
    http.csrf().disable();
    if (myConfig.getCheckJwt()) {
      http.authorizeRequests()
          .antMatchers("/**/cds-services/**", "/**/requests/**").authenticated()
          .anyRequest().permitAll()
          .and()
          .addFilter(new JwtAuthorizationFilter(authenticationManager(), requestService, publicKeyRepository));
    }else {
      http.headers().frameOptions().disable();
    }
  }

}