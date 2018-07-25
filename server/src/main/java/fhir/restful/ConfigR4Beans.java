package fhir.restful;

import ca.uhn.fhir.jpa.config.r4.BaseR4Config;
import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.dao.IFhirSystemDao;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import org.hl7.davinci.CoverageRequirementsDiscoveryOperation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;


import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableAutoConfiguration
public class ConfigR4Beans{


    @Bean
    public List<Object> mySystemProviderR4(){

        List<Object> plainProviders=new ArrayList<Object>();
        plainProviders.add(new CoverageRequirementsDiscoveryOperation());
        return plainProviders;
    }

//    @Bean(name = "mySystemDaoR4", autowire = Autowire.BY_NAME)
//    public IFhirSystemDao<Bundle, Meta> systemDaoR4() {
//        ca.uhn.fhir.jpa.dao.r4.FhirSystemDaoR4 retVal = new ca.uhn.fhir.jpa.dao.r4.FhirSystemDaoR4();
//        return retVal;
//    }


    @Bean(autowire = Autowire.BY_TYPE)
    public IServerInterceptor myLoggingInterceptor(){
        return new LoggingInterceptor();
    }

    @Bean(autowire = Autowire.BY_TYPE)
    public IServerInterceptor myAuthInterceptor(){
        return new AuthorizationInterceptor();
    }



}
