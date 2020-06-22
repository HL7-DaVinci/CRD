package org.hl7.davinci.endpoint.files.cdsconnect;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.hl7.davinci.endpoint.config.YamlConfig;
import org.hl7.davinci.endpoint.config.CdsConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;


@Component
@Profile("cdsConnect")
public class CdsConnectConnection {

  static final Logger logger = LoggerFactory.getLogger(CdsConnectConnection.class);

  String cookie;
  Date cookieExpiration;

  private String baseUrl;
  private String username;
  private String password;

  private Boolean useBasicAuth;
  private String basicAuthValue;

  private RestTemplate restTemplate;

  @Autowired
  public CdsConnectConnection(YamlConfig myConfig) {
    CdsConnect cdsConnectConfig = myConfig.getCdsConnect();
    this.baseUrl = cdsConnectConfig.getUrl();
    this.username = cdsConnectConfig.getUsername();
    this.password = cdsConnectConfig.getPassword();

    // use basic authorization if enabled
    if (cdsConnectConfig.getBasicAuth() != null) {
      useBasicAuth = true;
      logger.info("using basicAuth: " + cdsConnectConfig.getBasicAuth());
      basicAuthValue = Base64.getEncoder().encodeToString(cdsConnectConfig.getBasicAuth().getBytes());
    } else {
      useBasicAuth = false;
    }

    // configure the proxy if set
    if ((cdsConnectConfig.getProxyHost() != null) && (cdsConnectConfig.getProxyPort() != null)) {
      logger.info("using proxy: " + cdsConnectConfig.getProxyHost() + ":" + cdsConnectConfig.getProxyPort());

      SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

      Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP,
          new InetSocketAddress(cdsConnectConfig.getProxyHost(), cdsConnectConfig.getProxyPort()));
      requestFactory.setProxy(proxy);

      this.restTemplate = new RestTemplate(requestFactory);

    } else {
      this.restTemplate = new RestTemplate();
    }

   logger.info("CdsConnectConnection(): " + baseUrl);

    try {
      login();
    } catch (RestClientException e) {
      logger.warn("Unable to connect to server: " + baseUrl, e);
    }
  }

  public String getBaseUrl() { return baseUrl; }

  public boolean connected() {
    return (cookie != null);
  }

  public boolean cookieValid() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiration = LocalDateTime.ofInstant(cookieExpiration.toInstant(), ZoneId.systemDefault());
    return expiration.isAfter(now);
  }

  public void login() {
    if (connected() && cookieValid()) {
      // already logged in
      return;
    }

    logger.info("logging in");

    final String loginUrl = baseUrl + "/user/login?_format=json&=";

    // build the headers
    MultiValueMap<String, String> loginHeaders = new LinkedMultiValueMap<String, String>();
    Map loginMap = new HashMap();
    loginMap.put("Content-Type", "application/json");
    if (useBasicAuth) {
      loginMap.put("Authorization", "basic " + basicAuthValue);
    }
    loginHeaders.setAll(loginMap);

    Map requestBody = new HashMap();
    requestBody.put("name", username);
    requestBody.put("pass", password);

    HttpEntity<?> request = new HttpEntity<>(requestBody, loginHeaders);

    // attempt to login until successful
    int tries = 0;
    while ((cookie == null) && (tries < 100)) {
      // execute
      ResponseEntity<?> loginResponse = restTemplate.postForEntity(loginUrl, request, String.class);
      String loginResponseString = loginResponse.getBody().toString();

      HttpHeaders loginResponseHeaders = loginResponse.getHeaders();

      // get the authentication cookie
      cookie = loginResponseHeaders.getFirst(loginResponseHeaders.SET_COOKIE);
      if (cookie == null) {
        logout();
        tries++;
      }
    }

    // get the expiration date from the cookie
    String match = "expires=";
    String expirationDate = cookie.substring(cookie.indexOf(match)+match.length(), cookie.indexOf("Max-Age"));

    try {
      DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy kk:mm:ss z", Locale.ENGLISH);
      cookieExpiration = df.parse(expirationDate);

      logger.info("cookie expires: " + cookieExpiration.toString());
    } catch (ParseException e) {
      logger.warn("failed to parse expiration date: " + expirationDate);
    }
  }

  public void logout() {
    try {
      logger.info("logging out");
      cookie = null;
      String logoutUrl = baseUrl + "/user/logout";

      restExchange(logoutUrl, true, String.class);

    } catch (RestClientException e) {
      logger.warn("Not logged in to server: " + baseUrl);
    }
  }

  public <E> E restExchange(String url, boolean isRetry, Class<E> bodyClass) {
    if (isRetry) {
      logger.info("restExchange retry: " + url);
    }
    //logger.info("CdsConnectConnection::restExchange(): url: " + url);

    // login if necessary
    login();

    // build the headers
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.add("cookie", cookie);
    if (useBasicAuth) {
      headers.add("Authorization", "basic " + basicAuthValue);
    }

    HttpEntity<String> entity = new HttpEntity<>("", headers);

    try {
      ResponseEntity<E> response = restTemplate.exchange(url, HttpMethod.GET, entity, bodyClass);
      return response.getBody();

    } catch (HttpClientErrorException e) {
      if (isRetry) {
        // only retry with an attempt to log in once
        throw e;
      } else {
        // try again and make sure we are logged out
        logout();
        return restExchange(url, true, bodyClass);
      }
    }
  }

  public CdsConnectArtifactList queryForArtifactList() {
    logger.info("queryForArtifactList()");
    return new CdsConnectArtifactList(this, queryForArtifacts(""));
  }

  private String queryForArtifacts(String query) {
    logger.info("queryForArtifacts( " + query + " )");
    String fullUrl = baseUrl + "/erx_rules/" + query + "?_format=json";
    return restExchange(fullUrl, false, String.class);
  }

  public String retrieveArtifact(Integer nodeId) {
    logger.info("retrieveArtifact( " + nodeId.toString() + " )");
    String artifactUrl = baseUrl + "/cds_api/" + nodeId.toString();
    return restExchange(artifactUrl, false, String.class);
  }

  public byte[] retrieveCqlBundle(String cqlBundleLocation) {
    logger.info("retrieveCqlBundle( " + cqlBundleLocation + " )");
    String fileUrl = baseUrl + cqlBundleLocation;
    return restExchange(fileUrl, false, byte[].class);
  }
}
