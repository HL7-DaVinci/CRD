package org.hl7.davinci.ehrserver;

import java.util.Arrays;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import ca.uhn.fhir.jpa.search.LuceneSearchMappingFactory;
import ca.uhn.fhir.jpa.util.DerbyTenSevenHapiFhirDialect;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ca.uhn.fhir.jpa.config.BaseJavaConfigR4;
import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.util.SubscriptionsRequireManualActivationInterceptorR4;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.springframework.web.cors.CorsConfiguration;

/**
 * This is the primary configuration file for the example server.
 */
@Configuration
@EnableTransactionManagement()
public class FhirServerConfig extends BaseJavaConfigR4 {
  static final Logger logger = LoggerFactory.getLogger(FhirServerConfig.class);

  /**
   * Configure FHIR properties around the the JPA server via this bean.
   */
  @Bean()
  public DaoConfig daoConfig() {
    logger.info("FhirServerConfig::daoConfig()");
    DaoConfig retVal = new DaoConfig();
    retVal.setAllowMultipleDelete(true);
    return retVal;
  }

  /**
   * The following bean configures the database connection. The 'url' property value of
   * "jdbc:derby:directory:jpaserver_derby_files;create=true" indicates that the server should save resources in a
   * directory called "jpaserver_derby_files".
   * <p>
   * A URL to a remote database could also be placed here, along with login credentials and other properties supported
   * by BasicDataSource.
   */
  @Bean(destroyMethod = "close")
  public DataSource dataSource() {
    logger.info("FhirServerConfig::dataSource()");
    BasicDataSource retVal = new BasicDataSource();
    retVal.setDriver(new org.apache.derby.jdbc.EmbeddedDriver());
    retVal.setUrl("jdbc:derby:directory:target/jpaserver_derby_files;create=true");
    retVal.setUsername("");
    retVal.setPassword("");
    return retVal;
  }

  @Override
  @Bean()
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    logger.info("FhirServerConfig::entityManagerFactory()");
    LocalContainerEntityManagerFactoryBean retVal = super.entityManagerFactory();
    retVal.setPersistenceUnitName("HAPI_PU");
    retVal.setDataSource(dataSource());
    retVal.setJpaProperties(jpaProperties());
    return retVal;
  }

  private Properties jpaProperties() {
    logger.info("FhirServerConfig::jpaProperties()");
    Properties extraProperties = new Properties();
    extraProperties.put("hibernate.dialect", DerbyTenSevenHapiFhirDialect.class.getName());
    extraProperties.put("hibernate.format_sql", "true");
    extraProperties.put("hibernate.show_sql", "false");
    extraProperties.put("hibernate.hbm2ddl.auto", "update");
    extraProperties.put("hibernate.jdbc.batch_size", "20");
    extraProperties.put("hibernate.cache.use_query_cache", "false");
    extraProperties.put("hibernate.cache.use_second_level_cache", "false");
    extraProperties.put("hibernate.cache.use_structured_entries", "false");
    extraProperties.put("hibernate.cache.use_minimal_puts", "false");
    extraProperties.put("hibernate.search.model_mapping", LuceneSearchMappingFactory.class.getName());
    extraProperties.put("hibernate.search.default.directory_provider", "filesystem");
    extraProperties.put("hibernate.search.default.indexBase", "target/lucenefiles");
    extraProperties.put("hibernate.search.lucene_version", "LUCENE_CURRENT");
    //extraProperties.put("hibernate.search.default.worker.execution", "async");
    return extraProperties;
  }

  /**
   * Do some fancy logging to create a nice access log that has details about each incoming request.
   */
  public IServerInterceptor loggingInterceptor() {
    logger.info("FhirServerConfig::loggingInterceptor()");
    LoggingInterceptor retVal = new LoggingInterceptor();
    retVal.setLoggerName("fhirtest.access");
    retVal.setMessageFormat(
        "Path[${servletPath}] Source[${requestHeader.x-forwarded-for}] Operation[${operationType} "
            + "${operationName} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] "
            + "ResponseEncoding[${responseEncodingNoDefault}]");
    retVal.setLogExceptions(true);
    retVal.setErrorMessageFormat("ERROR - ${requestVerb} ${requestUrl}");
    return retVal;
  }

  /**
   * This interceptor adds some pretty syntax highlighting in responses when a browser is detected.
   */
  @Bean(autowire = Autowire.BY_TYPE)
  public IServerInterceptor responseHighlighterInterceptor() {
    logger.info("FhirServerConfig::responseHighlighterInterceptor()");
    return new ResponseHighlighterInterceptor();
  }

  @Bean(autowire = Autowire.BY_TYPE)
  public IServerInterceptor subscriptionSecurityInterceptor() {
    logger.info("FhirServerConfig::subscriptionSecurityInterceptor()");
    return new SubscriptionsRequireManualActivationInterceptorR4();
  }


  @Bean(autowire = Autowire.BY_TYPE)
  public IServerInterceptor authorizationInterceptor() {
    logger.info("FhirServerConfig::authorizationInterceptor");
    return new ClientAuthorizationInterceptor();
  }

  /**
   * This interceptor filters the headers, origin of the data, and the methods.
   */
  @Bean(autowire = Autowire.BY_TYPE)
  public IServerInterceptor corsInterceptor() {
    logger.info("FhirServerConfig::corsInterceptor");
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedHeader("x-fhir-starter");
    config.addAllowedHeader("Origin");
    config.addAllowedHeader("Accept");
    config.addAllowedHeader("X-Requested-With");
    config.addAllowedHeader("Content-Type");

    // Allow all origins for now since the port the server gets run on might change
    config.addAllowedOrigin("*");

    config.addExposedHeader("Location");
    config.addExposedHeader("Content-Location");
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

    // Create the interceptor and register it
    return new CorsInterceptor(config);
  }

  /**
   * Setup a new JPA transaction manager.
   */
  @Bean()
  public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    logger.info("FhirServerConfig::transactionManager()");
    JpaTransactionManager retVal = new JpaTransactionManager();
    retVal.setEntityManagerFactory(entityManagerFactory);
    return retVal;
  }

}
