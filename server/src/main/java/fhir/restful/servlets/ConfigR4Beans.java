package fhir.restful.servlets;

import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import org.hl7.davinci.CoverageRequirementsDiscoveryOperation;
import org.hl7.davinci.RestfulDaVinciEligibilityResponseProvider;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;


import java.util.ArrayList;
import java.util.List;

@Configuration
public class ConfigR4Beans {

    @Bean
    public List<Object> mySystemProviderR4(){

        List<Object> plainProviders=new ArrayList<Object>();
        plainProviders.add(new RestfulDaVinciEligibilityResponseProvider());
        plainProviders.add(new CoverageRequirementsDiscoveryOperation());
        return plainProviders;
    }


    @Bean(autowire = Autowire.BY_TYPE)
    public IServerInterceptor myInterceptors(){
        return new LoggingInterceptor();
    }

}
