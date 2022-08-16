package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.config.YamlConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class EndToEndRequestPrefetchTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(9089);
  private String deviceRequestFullPrefetchNoCoverage = FileUtils
      .readFileToString(new ClassPathResource("deviceRequestFullPrefetch_r4.json").getFile(),
          Charset.defaultCharset());
  private String deviceRequestFullPrefetchWithCoverage = FileUtils
      .readFileToString(new ClassPathResource("deviceRequestFullPrefetchWithCoverage_r4.json").getFile(),
          Charset.defaultCharset());
  private String deviceRequestEmptyPrefetchJson = FileUtils
      .readFileToString(new ClassPathResource("deviceRequestEmptyPrefetch_r4.json").getFile(),
          Charset.defaultCharset());
  private String deviceRequestPrefetchResponseJson = FileUtils
      .readFileToString(new ClassPathResource("deviceRequestPrefetchResponse_r4.json").getFile(),
          Charset.defaultCharset());
  private String prefetchUrlMatcher = "\\/DeviceRequest\\?_id=123"
      + "&_include=DeviceRequest:patient"
      + "&_include=DeviceRequest:performer"
      + "&_include=DeviceRequest:requester"
      + "&_include=DeviceRequest:device"
      + "&_include:iterate=PractitionerRole:organization"
      + "&_include:iterate=PractitionerRole:practitioner(.*?)"
      +"|\\/Coverage\\?patient=c2f0f972-5f84-4518-948f-63d00a1fa5a0";

  @LocalServerPort
  private int port;
  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private YamlConfig myConfig;

  public EndToEndRequestPrefetchTest() throws IOException {
  }

  @Test
  public void shouldRunSuccessfullyWithoutCoverage() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestFullPrefetchNoCoverage, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-sign-crd", entity,
            JsonNode.class);

    assertEquals(1, cards.get("cards").size());
  }

  @Test
  public void shouldRunSuccessfullyWithCoverage() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestFullPrefetchWithCoverage, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-sign-crd", entity,
            JsonNode.class);

    assertEquals(1, cards.get("cards").size());
  }

  @Test
  public void shouldSuccessfullyFillPreFetch() {
    // Disable Query Batch Request since it relies on a Fhir Server or mock class.
    myConfig.setQueryBatchRequest(false);
    stubFor(get(urlMatching(prefetchUrlMatcher))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(deviceRequestPrefetchResponseJson)));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestEmptyPrefetchJson, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-sign-crd", entity,
            JsonNode.class);

    System.out.println(cards);
    String summary = cards.get("cards").get(0).get("summary").textValue();
    assert(summary.endsWith("Documentation Required."));
  }

  @Test
  public void shouldFailToFillPrefetch() {
    // Disable Query Batch Request since it relies on a Fhir Server or mock class.
    myConfig.setQueryBatchRequest(false);
    stubFor(get(urlMatching(prefetchUrlMatcher))
        .willReturn(aResponse()
            .withStatus(404)));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestEmptyPrefetchJson, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-sign-crd", entity,
            JsonNode.class);

    System.out.println(cards);
    assertEquals(cards.get("cards").get(0).get("summary").textValue(),
        "Unable to (pre)fetch any supported bundles.");
  }
}
