package org.cdshooks;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.UUID;
import javax.validation.constraints.NotNull;

public abstract class CdsRequest<prefetchTypeT, serviceContextTypeT> {
  @NotNull(message = "unsupported hook")
  private Hook hook = null;

  @NotNull
  private UUID hookInstance = null;

  private String fhirServer = null;

  private FhirAuthorization fhirAuthorization = null;

  @NotNull
  private String user = null;

  @NotNull
  private serviceContextTypeT context = null;

  private prefetchTypeT prefetch = null;


  public prefetchTypeT getPrefetch() {
    return prefetch;
  }

  public void setPrefetch(prefetchTypeT prefetch) {
    this.prefetch = prefetch;
  }

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

  public FhirAuthorization getFhirAuthorization() {
    return fhirAuthorization;
  }

  public void setFhirAuthorization(FhirAuthorization oauth) {
    this.fhirAuthorization = oauth;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public serviceContextTypeT getContext() {
    return context;
  }

  public void setContext(serviceContextTypeT context) {
    this.context = context;
  }


  @JsonGetter("hookInstance")
  public String getHookInstanceAsString() {
    return hookInstance.toString();
  }


  /**
   * This should return a traversible structure that can be used to resolve prefetch tokens.
   * It is abstract since different hooks have different elements as prefetch tokens.
   *
   * @return A traversable object (traversable with PropertyUtils)
   */
  public abstract Object getDataForPrefetchToken();


}
