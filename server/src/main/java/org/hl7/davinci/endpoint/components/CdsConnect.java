package org.hl7.davinci.endpoint.components;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Class for retrieving CQL rules from CDS Connect.
 */
public class CdsConnect {

  static final Logger logger =
      LoggerFactory.getLogger(CdsConnect.class);

  private String baseUrl;

  private RestTemplate restTemplate;

  /**
   * Constructor that saves the baseURL and creates the RestTemplate.
   *
   * @param baseUrl is the URL that CDS Connect is located at
   */
  public CdsConnect(String baseUrl) {
    logger.info("CdsConnect(): " + baseUrl);
    this.baseUrl = baseUrl;
    this.restTemplate = new RestTemplate();
  }

  /**
   * Retrieve the CQL rules from CDS Connect.
   *
   * @param payer the payer's rules to retrieve (ie. cms)
   * @param code the device code
   * @param codeSystem the code system for the code provided (ie. cpt)
   * @return list of CQL files as strings
   */
  public List<String> getRules(String payer, String code, String codeSystem) {
    String query = payer + "/" + codeSystem + "/" + code;
    logger.info("CdsConnect::getRules(): " + query);
    List<String> rules = new ArrayList<>();

    try {

      String cookie = login("admin", "admin");

      String queryResults = queryForRules(cookie, query);

      try {
        // process all of the rules
        JsonArray jsonArray = new JsonParser().parse(queryResults).getAsJsonArray();
        if (jsonArray.isJsonArray()) {
          for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Integer nodeId = jsonObject.get("nid").getAsInt();

            String artifact = retrieveArtifact(cookie, nodeId);

            // process the files attached to the artifact
            JsonObject jsonArtifactObject = new JsonParser().parse(artifact).getAsJsonObject();
            JsonElement jsonFiles = jsonArtifactObject.get("artifact_representation").getAsJsonObject()
                .get("logic_files");
            if (jsonFiles.isJsonArray()) {
              JsonArray jsonFilesArray = jsonFiles.getAsJsonArray();
              for (JsonElement jsonFileElement : jsonFilesArray) {
                String cqlFileLocation = jsonFileElement.getAsString();

                rules.add(retrieveCqlFile(cookie, cqlFileLocation));
              }
            }
          }
        }
      } catch (JsonSyntaxException e) {
        e.printStackTrace();
      }

      logout(cookie);

    } catch (HttpClientErrorException e) {
      logger.warn("Not Logged In", e);
    } catch (RestClientException e) {
      logger.warn("Unable to connect to server: " + baseUrl, e);
    }

    return rules;
  }

  private String login(String username, String password) {
    logger.info("login()");
    final String loginUrl = baseUrl + "/user/login?_format=json&=";

    // build the headers
    MultiValueMap<String, String> loginHeaders = new LinkedMultiValueMap<String, String>();
    Map loginMap = new HashMap<String, String>();
    loginMap.put("Content-Type", "application/json");
    loginHeaders.setAll(loginMap);

    Map requestBody = new HashMap();
    requestBody.put("name", username);
    requestBody.put("pass", password);

    HttpEntity<?> request = new HttpEntity<>(requestBody, loginHeaders);

    // execute
    ResponseEntity<?> loginResponse = restTemplate.postForEntity(loginUrl, request, String.class);
    String loginResponseString = loginResponse.getBody().toString();
    //logger.info("login response: " + loginResponseString);

    HttpHeaders loginResponseHeaders = loginResponse.getHeaders();

    // get the authentication cookie
    String cookie = loginResponseHeaders.getFirst(loginResponseHeaders.SET_COOKIE);
    logger.info("cookie: " + cookie);
    return cookie;
  }

  private void logout(String cookie) {
    logger.info("logout()");
    String logoutUrl = baseUrl + "/user/logout";

    // build the headers
    HttpHeaders logoutHeaders = new HttpHeaders();
    logoutHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    logoutHeaders.add("cookie", cookie);
    HttpEntity<String> logoutEntity = new HttpEntity<>("", logoutHeaders);

    // execute
    ResponseEntity<String> logoutResponse = restTemplate.exchange(logoutUrl, HttpMethod.GET,
        logoutEntity, String.class);
  }

  private String queryForRules(String cookie, String query) {
    logger.info("queryForRules( " + query + " )");
    String fullUrl = baseUrl + "/views/rules/" + query + "?_format=json";

    // build the headers
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.add("cookie", cookie);

    HttpEntity<String> entity = new HttpEntity<>("", headers);

    // execute
    ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.GET,
        entity, String.class);
    String jsonResponseString = response.getBody();
    //logger.info("query results: " + jsonResponseString);

    return jsonResponseString;
  }

  private String retrieveArtifact(String cookie, Integer nodeId) {
    logger.info("retrieveArtifact( " + nodeId.toString() + " )");
    String artifactUrl = baseUrl + "/cds_api/" + nodeId.toString();

    // build the headers
    HttpHeaders artifactHeaders = new HttpHeaders();
    artifactHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    artifactHeaders.add("cookie", cookie);

    HttpEntity<String> artifactEntity = new HttpEntity<>("", artifactHeaders);

    // execute
    ResponseEntity<String> artifactResponse = restTemplate.exchange(artifactUrl, HttpMethod.GET,
        artifactEntity, String.class);
    String artifactResponseString = artifactResponse.getBody();
    //logger.info("artifact: " + artifactResponseString);

    return artifactResponseString;
  }

  private String retrieveCqlFile(String cookie, String cqlFileLocation) {
    logger.info("retrieveCqlFile( " + cqlFileLocation + " )");
    String fileUrl = baseUrl + cqlFileLocation;

    // build the headers
    HttpHeaders fileHeaders = new HttpHeaders();
    fileHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    fileHeaders.add("cookie", cookie);

    HttpEntity<String> fileEntity = new HttpEntity<>("", fileHeaders);

    // execute
    ResponseEntity<String> fileResponse = restTemplate.exchange(fileUrl, HttpMethod.GET,
        fileEntity, String.class);

    return fileResponse.getBody();
  }
}
