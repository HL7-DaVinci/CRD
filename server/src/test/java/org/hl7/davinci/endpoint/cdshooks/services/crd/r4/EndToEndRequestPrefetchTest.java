package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.hl7.davinci.endpoint.Application;
import org.junit.Ignore;
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
  private String deviceRequestEmptyPrefetchJson = FileUtils
      .readFileToString(new ClassPathResource("deviceRequestEmptyPrefetch_r4.json").getFile(),
          Charset.defaultCharset());
  private String deviceRequestPrefetchResponseJson = FileUtils
      .readFileToString(new ClassPathResource("deviceRequestPrefetchResponse_r4.json").getFile(),
          Charset.defaultCharset());
  private String prefetchUrlMatcher = "\\/DeviceRequest\\?_id=123.*";

  @LocalServerPort
  private int port;
  @Autowired
  private TestRestTemplate restTemplate;

  public EndToEndRequestPrefetchTest() throws IOException {
  }

  @Ignore("No CQL R4 Support at this time.") @Test
  public void shouldSuccessfullyFillPreFetch() {
    stubFor(get(urlMatching(prefetchUrlMatcher))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(deviceRequestPrefetchResponseJson)));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestEmptyPrefetchJson, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-review-crd", entity,
            JsonNode.class);

    System.out.println(cards);
    assertEquals(cards.get("cards").get(0).get("summary").textValue(),
        "No documentation is required for a device or service with code: E0250");
  }

  @Ignore("No CQL R4 Support at this time.") @Test
  public void shouldFailToFillPrefetch() {
    stubFor(get(urlMatching(prefetchUrlMatcher))
        .willReturn(aResponse()
            .withStatus(404)));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestEmptyPrefetchJson, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-review-crd", entity,
            JsonNode.class);

    System.out.println(cards);
    assertEquals(cards.get("cards").get(0).get("summary").textValue(),
        "Unable to (pre)fetch any supported resources from the bundle.");
  }
}
