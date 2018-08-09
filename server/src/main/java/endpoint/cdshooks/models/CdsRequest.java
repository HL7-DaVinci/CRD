package endpoint.cdshooks.models;

import java.util.UUID;
import javax.validation.constraints.NotNull;

public class CdsRequest {
  @NotNull(message = "unsupported hook")
  private Hook hook = null;

  @NotNull private UUID hookInstance = null;

  private String fhirServer = null;

  private Object oauth = null;

  @NotNull private String user = null;

  private String patient = null;

  private String encounter = null;

  //  @NotNull TODO: why does this break validation if we extend this class???
  private Object context = null;

  private Object prefetch = null;

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

  public String getPatient() {
    return patient;
  }

  public void setPatient(String patient) {
    this.patient = patient;
  }

  public String getEncounter() {
    return encounter;
  }

  public void setEncounter(String encounter) {
    this.encounter = encounter;
  }

  public Object getContext() {
    return context;
  }

  public void setContext(Object context) {
    this.context = context;
  }

  public Object getPrefetch() {
    return prefetch;
  }

  public void setPrefetch(Object prefetch) {
    this.prefetch = prefetch;
  }
}
