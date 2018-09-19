package org.hl7.davinci.ehrServer;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.provider.r4.JpaConformanceProviderR4;
import ca.uhn.fhir.jpa.provider.r4.JpaSystemProviderR4;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.*;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;

public class EhrServer extends RestfulServer {
  static final Logger logger = LoggerFactory.getLogger(EhrServer.class);

  private static final long serialVersionUID = 1L;

  private WebApplicationContext myAppCtx;

  @SuppressWarnings("unchecked")
  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    logger.info("EhrServer::initialize() start");
    /*
     * We want to support FHIR R4 format. This means that the server
     * will use the R4 bundle format and other R4 encoding changes.
     */
    FhirVersionEnum fhirVersion = FhirVersionEnum.R4;
    setFhirContext(new FhirContext(fhirVersion));

    // Get the spring context from the web container (it's declared in web.xml)
    myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();

    /*
     * The BaseJavaConfigR4.java class is a spring configuration
     * file which is automatically generated as a part of hapi-fhir-jpaserver-base and
     * contains bean definitions for a resource provider for each resource type
     */
    String resourceProviderBeanName = "myResourceProvidersR4";

    List<IResourceProvider> beans = myAppCtx.getBean(resourceProviderBeanName, List.class);
    setResourceProviders(beans);

    /*
     * The system provider implements non-resource-type methods, such as
     * transaction, and global history.
     */
    Object systemProvider = myAppCtx.getBean("mySystemProviderR4", JpaSystemProviderR4.class);
    setPlainProviders(systemProvider);

    /*
     * The conformance provider exports the supported resources, search parameters, etc for
     * this server. The JPA version adds resource counts to the exported statement, so it
     * is a nice addition.
     */
    IFhirSystemDao<Bundle, Meta> systemDao = myAppCtx.getBean("mySystemDaoR4", IFhirSystemDao.class);
    JpaConformanceProviderR4 confProvider = new JpaConformanceProviderR4(this, systemDao,
        myAppCtx.getBean(DaoConfig.class));
    confProvider.setImplementationDescription("EHR Server");
    setServerConformanceProvider(confProvider);

    /*
     * Enable ETag Support (this is already the default)
     */
    setETagSupport(ETagSupportEnum.ENABLED);

    /*
     * This server tries to dynamically generate narratives
     */
    FhirContext ctx = getFhirContext();
    ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());

    /*
     * Default to JSON and pretty printing
     */
    setDefaultPrettyPrint(true);
    setDefaultResponseEncoding(EncodingEnum.JSON);

    /*
     * -- New in HAPI FHIR 1.5 --
     * This configures the server to page search results to and from
     * the database, instead of only paging them to memory. This may mean
     * a performance hit when performing searches that return lots of results,
     * but makes the server much more scalable.
     */
    setPagingProvider(myAppCtx.getBean(DatabaseBackedPagingProvider.class));

    /*
     * Load interceptors for the server from Spring (these are defined in FhirServerConfig.java)
     */
    Collection<IServerInterceptor> interceptorBeans = myAppCtx.getBeansOfType(IServerInterceptor.class).values();
    for (IServerInterceptor interceptor : interceptorBeans) {
      this.registerInterceptor(interceptor);
    }

    /*
     * If you are hosting this server at a specific DNS name, the server will try to
     * figure out the FHIR base URL based on what the web container tells it, but
     * this doesn't always work. If you are setting links in your search bundles that
     * just refer to "localhost", you might want to use a server address strategy:
     */
    //setServerAddressStrategy(new HardcodedServerAddressStrategy("http://mydomain.com/fhir/baseDstu2"));

    /*
     * If you are using R4+, you may want to add a terminology uploader, which allows
     * uploading of external terminologies such as Snomed CT. Note that this uploader
     * does not have any security attached (any anonymous user may use it by default)
     * so it is a potential security vulnerability. Consider using an AuthorizationInterceptor
     * with this feature.
     */
    //if (fhirVersion == FhirVersionEnum.R4) {
    //	 registerProvider(myAppCtx.getBean(TerminologyUploaderProviderR4.class));
    //}
    logger.info("EhrServer::initialize() end");
  }

}
