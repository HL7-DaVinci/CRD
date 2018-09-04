package org.hl7.davinci.cdshooks;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.annotation.JsonGetter;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.hl7.fhir.instance.model.api.IBaseResource;

public abstract class CdsRequest {
  @NotNull(message = "unsupported hook")
  private Hook hook = null;

  @NotNull private UUID hookInstance = null;

  private String fhirServer = null;

  private Object oauth = null;

  @NotNull private String user = null;

  //  @NotNull TODO: why does this break validation if we extend this class???
  private Object context = null;

  public CrdPrefetch getPrefetch() {
    return prefetch;
  }

  public void setPrefetch(CrdPrefetch prefetch) {
    this.prefetch = prefetch;
  }

  private CrdPrefetch prefetch = null;

  public Hook getHook() {
    return hook;
  }

  public void setHook(Hook hook) {
    this.hook = hook;
  }

  public UUID getHookInstance() {
    return hookInstance;
  }

  public void setHookInstance(UUID hookInstance) {
    this.hookInstance = hookInstance;
  }

  public String getFhirServer() {
    return fhirServer;
  }

  public void setFhirServer(String fhirServer) {
    this.fhirServer = fhirServer;
  }

  public Object getOauth() {
    return oauth;
  }

  public void setOauth(Object oauth) {
    this.oauth = oauth;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Object getContext() {
    return context;
  }

  public void setContext(Object context) {
    this.context = context;
  }


  @JsonGetter("hookInstance")
  public String getHookInstanceAsString() {
    return hookInstance.toString();
  }


  /**
   * This should return a traversible structure that can be used to resolve prefetch tokens.
   * It is abstract since different hooks have different elements as prefetch tokens.
   * @return A traversible object (traversible with PropertyUtils)
   */
  public abstract Object getDataForPrefetchToken();


}
