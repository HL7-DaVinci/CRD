package org.hl7.davinci.endpoint.config;

public class CdsConnect {
  private String url;
  private String username;
  private String password;
  private String basicAuth;
  private String proxyHost;
  private Integer proxyPort;

  public String getUrl() { return url; }

  public void setUrl(String url) { this.url = url; }

  public String getUsername() { return username; }

  public void setUsername(String username) { this.username = username; }

  public String getPassword() { return password; }

  public void setPassword(String password) { this.password = password; }

  public String getBasicAuth() { return basicAuth; }

  public void setBasicAuth(String basicAuth) { this.basicAuth = basicAuth; }

  public String getProxyHost() { return proxyHost; }

  public void setProxyHost(String proxyHost) { this.proxyHost = proxyHost; }

  public Integer getProxyPort() { return proxyPort; }

  public void setProxyPort(Integer proxyPort) { this.proxyPort = proxyPort; }
}
