package org.hl7.davinci.endpoint.rems;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.hl7.davinci.endpoint.rems.providers.QuestionnaireProvider;
import org.springframework.beans.factory.annotation.Autowired;

@WebServlet("/rems/fhir/*")
public class RemsServer extends RestfulServer {


    @Autowired
    QuestionnaireProvider questionnaireProvider;

    @Override
    protected void initialize() throws ServletException {
        // Create a context for the appropriate version
        setFhirContext(FhirContext.forR4());

        // Register resource providers
        registerProvider(questionnaireProvider);

        // Format the responses in nice HTML
        registerInterceptor(new ResponseHighlighterInterceptor());

    }
}