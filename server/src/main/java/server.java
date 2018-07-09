import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import org.hl7.davinci.RestfulDaVinciEligibilityResponseProvider;
import org.hl7.davinci.CoverageRequirementsDiscoveryOperation;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.List;

/**
 * In this example, we are using Servlet 3.0 annotations to define
 * the URL pattern for this servlet, but we could also
 * define this in a web.xml file.
 */
@WebServlet(urlPatterns= {"/fhir/*"}, displayName="DaVinci FHIR Server")
public class server extends RestfulServer {

    private static final long serialVersionUID = 1L;

    /**
     * The initialize method is automatically called when the servlet is starting up, so it can
     * be used to configure the servlet to define resource providers, or set up
     * configuration, interceptors, etc.
     */
    @Override
    protected void initialize() throws ServletException {

        FhirContext ctxR4 = FhirContext.forR4();
        setFhirContext(ctxR4);


        /*
         * The servlet defines any number of resource providers, and
         * configures itself to use them by calling
         * setResourceProviders()
         */

        List<Object> plainProviders=new ArrayList<Object>();
        plainProviders.add(new RestfulDaVinciEligibilityResponseProvider());
        plainProviders.add(new CoverageRequirementsDiscoveryOperation());
        setPlainProviders(plainProviders);

//        List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
//        resourceProviders.add(new RestfulDaVinciEligibilityResponseProvider());
//        setResourceProviders(resourceProviders);


        // Now register the logging interceptor
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        registerInterceptor(loggingInterceptor);

        // The SLF4j logger "test.accesslog" will receive the logging events
        loggingInterceptor.setLoggerName("test.accesslog");

        // This is the format for each line. A number of substitution variables may
        // be used here. See the JavaDoc for LoggingInterceptor for information on
        // what is available.
        loggingInterceptor.setMessageFormat("Source[${remoteAddr}] Operation[${operationType} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}]");

    }

}