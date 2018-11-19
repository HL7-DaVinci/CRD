package org.hl7.davinci.ehrserver;

import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.provider.r4.JpaConformanceProviderR4;
import ca.uhn.fhir.rest.server.RestfulServer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.UriType;

import javax.servlet.http.HttpServletRequest;


public class ServerConformance extends JpaConformanceProviderR4 {

  public ServerConformance(RestfulServer theRestfulServer, IFhirSystemDao<Bundle, Meta> theSystemDao, DaoConfig theDaoConfig) {
    super(theRestfulServer, theSystemDao, theDaoConfig);
  }

  @Override
  public CapabilityStatement getServerConformance(HttpServletRequest theRequest) {
    Extension securityExtension = new Extension();
    securityExtension.setUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");
    securityExtension.addExtension()
        .setUrl("authorize")
        .setValue(new UriType(Config.get("oauth_authorize")));
    securityExtension.addExtension()
        .setUrl("token")
        .setValue(new UriType(Config.get("oauth_token")));
    CapabilityStatement.CapabilityStatementRestSecurityComponent securityComponent = new CapabilityStatement.CapabilityStatementRestSecurityComponent();
    securityComponent.setCors(true);
    securityComponent
        .addExtension(securityExtension);

    CapabilityStatement regularConformance = super.getServerConformance(theRequest);
    regularConformance.getRest().get(0).setSecurity(securityComponent);
    return regularConformance;
  }
}
