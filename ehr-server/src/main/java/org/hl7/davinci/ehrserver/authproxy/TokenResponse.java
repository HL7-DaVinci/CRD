package org.hl7.davinci.ehrserver.authproxy;

public class TokenResponse {
  private String access_token;
  private String token_type;
  private int expires_in;
  private String scope;
  private String refresh_token;
  private String patient;
  private String appContext;




  public String getAppContext() {
    return appContext;
  }

  public TokenResponse setAppContext(String appContext) {
    this.appContext = appContext;
    return this;

  }

  public String getAccess_token() {
    return access_token;
  }

  public TokenResponse setAccess_token(String access_token) {
    this.access_token = access_token;
    return this;

  }

  public String getToken_type() {
    return token_type;
  }

  public TokenResponse setToken_type(String token_type) {
    this.token_type = token_type;
    return this;

  }

  public int getExpires_in() {
    return expires_in;
  }

  public TokenResponse setExpires_in(int expires_in) {
    this.expires_in = expires_in;
    return this;

  }

  public String getScope() {
    return scope;
  }

  public TokenResponse setScope(String scope) {
    this.scope = scope;
    return this;

  }

  public String getRefresh_token() {
    return refresh_token;
  }

  public TokenResponse setRefresh_token(String refresh_token) {
    this.refresh_token = refresh_token;
    return this;
  }

  public String getPatient() {
    return patient;
  }

  public TokenResponse setPatient(String patient) {
    this.patient = patient;
    return this;
  }
}
