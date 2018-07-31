package fhir.restful;

import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;

import java.util.ArrayList;
import java.util.List;

import org.hl7.davinci.CoverageRequirementsDiscoveryOperation;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
@EnableAutoConfiguration
public class ConfigR4Beans {


  /**
   * A bean to get the system providers supported by the fhir server.
   * @return a list containing the system providers to support
   */
  @Bean
  public List<Object> mySystemProviderR4() {

    List<Object> plainProviders = new ArrayList<Object>();
    plainProviders.add(new CoverageRequirementsDiscoveryOperation());
    return plainProviders;
  }

  @Bean(autowire = Autowire.BY_TYPE)
  public IServerInterceptor myLoggingInterceptor() {
    return new LoggingInterceptor();
  }

  @Bean(autowire = Autowire.BY_TYPE)
  public IServerInterceptor myAuthInterceptor() {
    return new AuthorizationInterceptor();
  }


}
