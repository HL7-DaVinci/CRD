package endpoint.servlets;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import endpoint.CoverageRequirementsDiscoveryOperation;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
//import fhir.restful.DaVinciAuthInterceptor;
import org.hl7.davinci.DaVinciEligibilityRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * In this example, we are using Servlet 3.0 annotations to define the URL pattern for this servlet,
 * but we could also define this in a web.xml file.
 */
@WebServlet(
    urlPatterns = {"/fhir/*"},
    displayName = "DaVinci FHIR Server")
public class FhirServlet extends RestfulServer {
  private static final long serialVersionUID = 1L;

  @Autowired
  CoverageRequirementsDiscoveryOperation crdOperation;

  /**
   * The initialize method is automatically called when the servlet is starting up, so it can be
   * used to configure the servlet to define resource providers, or set up configuration,
   * interceptors, etc.
   */
  @Override
  protected void initialize() throws ServletException {

    FhirContext ctxR4 = FhirContext.forR4();
    setFhirContext(ctxR4);

    // This is needed to correctly cast an incoming DaVinciEligibilityRequest, url must match that
    // defined in the resource profile
    ctxR4.setDefaultTypeForProfile(
        "http://base.url/DaVinciEligibilityRequest", DaVinciEligibilityRequest.class);

    /*
     * The servlet defines any number of resource providers, and
     * configures itself to use them by calling
     * setResourceProviders()
     */

    List<Object> plainProviders = new ArrayList<Object>();
    plainProviders.add(crdOperation);
    setPlainProviders(plainProviders);

    // Now register the logging interceptor
    LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
    // DaVinciAuthInterceptor authInterceptor = new DaVinciAuthInterceptor();
    // registerInterceptor(authInterceptor);
    registerInterceptor(loggingInterceptor);

    // The SLF4j logger "test.accesslog" will receive the logging events
    loggingInterceptor.setLoggerName("test.accesslog");

    // This is the format for each line. A number of substitution variables may
    // be used here. See the JavaDoc for LoggingInterceptor for information on
    // what is available.
    loggingInterceptor.setMessageFormat(
        "Source[${remoteAddr}] Operation[${operationType} "
            + "${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}]");
  }
}
