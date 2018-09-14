package org.cdshooks;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.UUID;
import javax.validation.constraints.NotNull;

public abstract class CdsRequest<ContextType, PrefetchType> {
  @NotNull(message = "unsupported hook")
  private Hook hook = null;

  @NotNull private UUID hookInstance = null;

  private String fhirServer = null;

  private Object oauth = null;

  @NotNull private String user = null;

  @NotNull private ContextType context = null;

  private PrefetchType prefetch = null;


  public PrefetchType getPrefetch() {
    return prefetch;
  }

  public void setPrefetch(PrefetchType prefetch) {
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

  public ContextType getContext() {
    return context;
  }

  public void setContext(ContextType context) {
    this.context = context;
  }


  @JsonGetter("hookInstance")
  public String getHookInstanceAsString() {
    return hookInstance.toString();
  }


  /**
   * This should return a traversible structure that can be used to resolve prefetch tokens.
   * It is abstract since different hooks have different elements as prefetch tokens.
   * @return A traversable object (traversable with PropertyUtils)
   */
  public abstract Object getDataForPrefetchToken();


}
