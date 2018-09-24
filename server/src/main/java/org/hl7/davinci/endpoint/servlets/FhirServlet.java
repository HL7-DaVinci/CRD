package org.hl7.davinci.endpoint.servlets;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import org.springframework.web.cors.CorsConfiguration;



//import fhir.restful.DaVinciAuthInterceptor;

/**
 * In this example, we are using Servlet 3.0 annotations to define the URL pattern for this servlet,
 * but we could also define this in a web.xml file.
 */
@WebServlet(
    urlPatterns = {"/fhir/*"},
    displayName = "DaVinci FHIR Server")
public class FhirServlet extends RestfulServer {
  private static final long serialVersionUID = 1L;


  /**
   * The initialize method is automatically called when the servlet is starting up, so it can be
   * used to configure the servlet to define resource providers, or set up configuration,
   * interceptors, etc.
   */
  @Override
  protected void initialize() throws ServletException {

    FhirContext ctxR4 = FhirContext.forR4();
    setFhirContext(ctxR4);

    List<Object> plainProviders = new ArrayList<Object>();
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
    CorsInterceptor interceptor = new CorsInterceptor(config);
    registerInterceptor(interceptor);
  }
}
