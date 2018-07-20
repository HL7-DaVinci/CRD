package fhir.restful.servlets;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.provider.dstu3.JpaSystemProviderDstu3;
import ca.uhn.fhir.jpa.provider.r4.JpaConformanceProviderR4;
import ca.uhn.fhir.jpa.provider.r4.JpaSystemProviderR4;
import ca.uhn.fhir.jpa.provider.r4.TerminologyUploaderProviderR4;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.ETagSupportEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.IncomingRequestAddressStrategy;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import org.hl7.davinci.CoverageRequirementsDiscoveryOperation;
import org.hl7.davinci.RestfulDaVinciEligibilityResponseProvider;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Meta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@WebServlet( "/fhir/*" )
public class FhirServlet extends RestfulServer {

    private static final long serialVersionUID = 3341258540126825379L;
    private final WebApplicationContext myAppCtx;

    public FhirServlet( WebApplicationContext myAppCtx ) {
        this.myAppCtx = myAppCtx;
    }

    /**
     * Use the ConfigR4Beans.java file to add beans, which can be referenced here or elsewhere
     * @throws ServletException
     */
    @Override
    protected void initialize() throws ServletException {
        WebApplicationContext parentAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
        this.setFhirContext( FhirContext.forR4() );
        this.setServerAddressStrategy( new IncomingRequestAddressStrategy() );
        this.setDefaultPrettyPrint( true );
        this.setDefaultResponseEncoding( EncodingEnum.JSON );
        this.setETagSupport( ETagSupportEnum.ENABLED );

        /*
         * The BaseJavaConfigDstu3.java class is a spring configuration
         * file which is automatically generated as a part of hapi-fhir-jpaserver-base and
         * contains bean definitions for a resource provider for each resource type
         */


//        myAppCtx.getServletContext(parentAppCtx);
//        myAppCtx.setServletConfig(getServletConfig());
//        myAppCtx.refresh();
//        List<IResourceProvider> beans = myAppCtx.getBean( "myResourceProvidersR4", List.class );
//        setResourceProviders( beans );
//        setResourceProviders();

        /*
         * The system provider implements non-resource-type methods, such as
         * transaction, and global history.
         */
        setPlainProviders( myAppCtx.getBean( "mySystemProviderR4", List.class ) );

        /*
         * The conformance provider exports the supported resources, search parameters, etc for
         * this server. The JPA version adds resource counts to the exported statement, so it
         * is a nice addition.
         */
//        IFhirSystemDao<Bundle, Meta> systemDao = myAppCtx.getBean( "mySystemDaoR4", IFhirSystemDao.class );
//        JpaConformanceProviderR4 confProvider = new JpaConformanceProviderR4( this, systemDao, myAppCtx.getBean( DaoConfig.class ) );
//        confProvider.setImplementationDescription( "Example Server" );
//        setServerConformanceProvider( confProvider );

        /*
         * This server tries to dynamically generate narratives
         */

        //getFhirContext().setNarrativeGenerator( new DefaultThymeleafNarrativeGenerator() );

        /*
         * -- New in HAPI FHIR 1.5 --
         * This configures the server to page search results to and from
         * the database, instead of only paging them to memory. This may mean
         * a performance hit when performing searches that return lots of results,
         * but makes the server much more scalable.
         */

        //setPagingProvider( myAppCtx.getBean( DatabaseBackedPagingProvider.class ) );

        /*
         * Load interceptors for the server from Spring (these are defined in FhirServerConfig.java)
         */
        Collection<IServerInterceptor> interceptorBeans = myAppCtx.getBeansOfType( IServerInterceptor.class ).values();
        for ( IServerInterceptor interceptor : interceptorBeans ) {
            this.registerInterceptor( interceptor );
        }

        /*
         * If you are hosting this server at a specific DNS name, the server will try to
         * figure out the FHIR base URL based on what the web container tells it, but
         * this doesn't always work. If you are setting links in your search bundles that
         * just refer to "localhost", you might want to use a server address strategy:
         */
        //setServerAddressStrategy(new HardcodedServerAddressStrategy("http://example.com/fhir/baseDstu2"));

        /*
         * If you are using DSTU3+, you may want to add a terminology uploader, which allows
         * uploading of external terminologies such as Snomed CT. Note that this uploader
         * does not have any security attached (any anonymous user may use it by default)
         * so it is a potential security vulnerability. Consider using an AuthorizationInterceptor
         * with this feature.
         */
        //registerProvider( myAppCtx.getBean( TerminologyUploaderProviderR4.class ) );
    }


}