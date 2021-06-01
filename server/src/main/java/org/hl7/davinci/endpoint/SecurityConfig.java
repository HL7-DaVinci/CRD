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

import java.util.List;
import java.util.logging.Logger;


@Configuration
@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {
  private static Logger logger = Logger.getLogger(SecurityConfig.class.getName());

  @Autowired
  private YamlConfig myConfig;

  @Autowired
  private RequestService requestService;

  @Autowired
  private PublicKeyRepository publicKeyRepository;

  /**
   * The CORS preflight must be accepted here or it will get rejected by the
   * Auth filter.  General CORS settings can be set here.
   * @return CORS configuration object
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(myConfig.getCorsOrigins());
    List<String> allowedOrigins = configuration.getAllowedOrigins();
    allowedOrigins.forEach((n) -> logger.info("CORS Allowed Origin: " + n));
    configuration.setAllowedMethods(ImmutableList.of("HEAD",
        "GET", "POST", "PUT", "DELETE", "PATCH"));
    // setAllowCredentials(true) is important, otherwise:
    // The value of the 'Access-Control-Allow-Origin' header in the
    // response must not be the wildcard '*' when the request's credentials
    // mode is 'include'.
    configuration.setAllowCredentials(true);
    // setAllowedHeaders is important! Without it, OPTIONS preflight request
    // will fail with 403 Invalid CORS request
    configuration.setAllowedHeaders(ImmutableList.of("Authorization", "Cache-Control", "Content-Type"));
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

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