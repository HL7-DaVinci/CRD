import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.hl7.davinci.endpoint.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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

import java.io.IOException;
import java.nio.charset.Charset;


@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class PrefetchIntegrationTest {

  private String medicationRequestPrefetch = FileUtils
      .readFileToString(new ClassPathResource("medicationRequestPrefetch.json").getFile(),
          Charset.defaultCharset());
  private String medicationRequestNoPrefetch = FileUtils
      .readFileToString(new ClassPathResource("medicationRequestNoPrefetch.json").getFile(),
          Charset.defaultCharset());

  private String medicationRequestPrefetchNoDoc = FileUtils
      .readFileToString(new ClassPathResource("medicationRequestPrefetchNoDoc.json").getFile(),
          Charset.defaultCharset());
  private String medicationRequestNoPrefetchNoDoc = FileUtils
      .readFileToString(new ClassPathResource("medicationRequestNoPrefetchNoDoc.json").getFile(),
          Charset.defaultCharset());

  private String deviceRequestPrefetch = FileUtils
      .readFileToString(new ClassPathResource("deviceRequestPrefetch.json").getFile(),
          Charset.defaultCharset());

  private String deviceRequestNoPrefetch = FileUtils
      .readFileToString(new ClassPathResource("deviceRequestNoPrefetch.json").getFile(),
          Charset.defaultCharset());

  private String deviceRequestPrefetchNoDoc = FileUtils
      .readFileToString(new ClassPathResource("deviceRequestPrefetchNoDoc.json").getFile(),
          Charset.defaultCharset());
  private String deviceRequestNoPrefetchNoDoc = FileUtils
      .readFileToString(new ClassPathResource("deviceRequestNoPrefetchNoDoc.json").getFile(),
          Charset.defaultCharset());

  @LocalServerPort
  private int port;
  @Autowired
  private TestRestTemplate restTemplate;

  public PrefetchIntegrationTest() throws IOException {
  }


  public void setup(String resourceFile, String bundleName) {
    JsonObject prefetchResources =  new JsonParser().parse(resourceFile).getAsJsonObject();
    JsonArray entries = prefetchResources.get("prefetch")
        .getAsJsonObject().get(bundleName)
        .getAsJsonObject().get("entry").getAsJsonArray();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    for( JsonElement entry : entries) {

      JsonObject resource = entry.getAsJsonObject().get("resource").getAsJsonObject();
      if(!resource.isJsonNull()){
        String resourceType = resource.getAsJsonPrimitive("resourceType").getAsString();
        String id = resource.getAsJsonPrimitive("id").getAsString();
        HttpEntity<String> entity = new HttpEntity<String>(resource
            .getAsJsonObject().toString(), headers);
        ResponseEntity<String> project= restTemplate.exchange(
            "http://localhost:8080/ehr-server/r4/"+resourceType+"/"+id+"/",
            HttpMethod.PUT,
            entity,
            String.class);
        System.out.print(resourceType + ": ");
        System.out.println(project.getStatusCode());
      }

    }
  }

  @Test
  public void testMedicationPrescribeNoPrefetchDocReq() {
    setup(medicationRequestPrefetch, "medicationRequestBundle");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(medicationRequestNoPrefetch, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/medication-prescribe-crd", entity,
            JsonNode.class);
    String success = "Documentation is required for the desired device or service";
    assert(success.equals(cards.get("cards").get(0).get("summary").asText()));
  }

  @Test
  public void testMedicationPrescribeWithPrefetchDocReq() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(medicationRequestPrefetch, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/medication-prescribe-crd", entity,
            JsonNode.class);
    String success = "Documentation is required for the desired device or service";
    assert(success.equals(cards.get("cards").get(0).get("summary").asText()));
  }

  @Test
  public void testMedicationPrescribeNoPrefetchNoDocReq() {
    setup(medicationRequestPrefetchNoDoc, "medicationRequestBundle");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(medicationRequestNoPrefetchNoDoc, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/medication-prescribe-crd", entity,
            JsonNode.class);
    String success = "No documentation is required for a device or service with code: 209431";
    System.out.println(cards);
    assert(success.equals(cards.get("cards").get(0).get("summary").asText()));
  }

  @Test
  public void testMedicationPrescribePrefetchNoDocReq() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(medicationRequestPrefetchNoDoc, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/medication-prescribe-crd", entity,
            JsonNode.class);
    String success = "No documentation is required for a device or service with code: 209431";
    System.out.println(cards);
    assert(success.equals(cards.get("cards").get(0).get("summary").asText()));
  }

  @Test
  public void testDeviceRequestNoPrefetchDocReq() {
    setup(deviceRequestPrefetch, "deviceRequestBundle");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestNoPrefetch, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-review-crd", entity,
            JsonNode.class);
    String success = "Documentation is required for the desired device or service";
    assert(success.equals(cards.get("cards").get(0).get("summary").asText()));
  }

  @Test
  public void testDeviceRequestWithPrefetchDocReq() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestPrefetch, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-review-crd", entity,
            JsonNode.class);
    String success = "Documentation is required for the desired device or service";
    assert(success.equals(cards.get("cards").get(0).get("summary").asText()));
  }

  @Test
  public void testDeviceRequestPrefetchNoDocReq() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestPrefetchNoDoc, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-review-crd", entity,
            JsonNode.class);
    String success = "No documentation is required for a device or service with code: A5500";
    System.out.println(cards);
    assert(success.equals(cards.get("cards").get(0).get("summary").asText()));
  }


  @Test
  public void testDeviceRequestNoPrefetchNoDocReq() {
    setup(deviceRequestPrefetchNoDoc, "deviceRequestBundle");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestNoPrefetchNoDoc, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-review-crd", entity,
            JsonNode.class);
    String success = "No documentation is required for a device or service with code: A5500";
    assert(success.equals(cards.get("cards").get(0).get("summary").asText()));
  }
}
