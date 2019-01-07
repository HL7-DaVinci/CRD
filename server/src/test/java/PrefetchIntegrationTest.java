import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.hl7.davinci.endpoint.Application;
import org.junit.Before;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class PrefetchIntegrationTest {

  private String deviceRequestPrefetch = FileUtils
      .readFileToString(new ClassPathResource("medicationRequest.json").getFile(),
          Charset.defaultCharset());
  private String deviceRequestNoPrefetch = FileUtils
      .readFileToString(new ClassPathResource("medicationRequestNoPrefetch.json").getFile(),
          Charset.defaultCharset());
  private String prefetchUrlMatcher = "\\/DeviceRequest\\?_id=123.*";

  @LocalServerPort
  private int port;
  @Autowired
  private TestRestTemplate restTemplate;

  public PrefetchIntegrationTest() throws IOException {
  }
  @Before
  public void setup(){
    JsonObject prefetchResources =  new JsonParser().parse(deviceRequestPrefetch).getAsJsonObject();
    JsonArray entries = prefetchResources.get("prefetch")
        .getAsJsonObject().get("medicationRequestBundle")
        .getAsJsonObject().get("entry").getAsJsonArray();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    for( JsonElement entry : entries) {
      JsonObject resource = entry.getAsJsonObject().get("resource").getAsJsonObject();
      String resourceType = resource.getAsJsonPrimitive("resourceType").getAsString();
      String id = resource.getAsJsonPrimitive("id").getAsString();
      HttpEntity<String> entity = new HttpEntity<String>(resource
          .getAsJsonObject().toString(), headers);
     ResponseEntity<String> project= restTemplate.exchange(
         "http://localhost:8080/ehr-server/r4/"+resourceType+"/"+id+"/",
         HttpMethod.PUT,
         entity,
         String.class);
     System.out.println(project.getStatusCode());
    }
  }

  @Test
  public void test1() {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestNoPrefetch, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/medication-prescribe-crd", entity,
            JsonNode.class);

    System.out.println(cards);
  }
}