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
      .readFileToString(new ClassPathResource("requests/medicationRequestPrefetch.json").getFile(),
          Charset.defaultCharset());
  private String medicationRequestNoPrefetch = FileUtils
      .readFileToString(new ClassPathResource("requests/medicationRequestNoPrefetch.json").getFile(),
          Charset.defaultCharset());

  private String medicationRequestPrefetchNoDoc = FileUtils
      .readFileToString(new ClassPathResource("requests/medicationRequestPrefetchNoDoc.json").getFile(),
          Charset.defaultCharset());
  private String medicationRequestNoPrefetchNoDoc = FileUtils
      .readFileToString(new ClassPathResource("requests/medicationRequestNoPrefetchNoDoc.json").getFile(),
          Charset.defaultCharset());

  private String deviceRequestPrefetch = FileUtils
      .readFileToString(new ClassPathResource("requests/deviceRequestPrefetch.json").getFile(),
          Charset.defaultCharset());

  private String deviceRequestNoPrefetch = FileUtils
      .readFileToString(new ClassPathResource("requests/deviceRequestNoPrefetch.json").getFile(),
          Charset.defaultCharset());

  private String deviceRequestPrefetchNoDoc = FileUtils
      .readFileToString(new ClassPathResource("requests/deviceRequestPrefetchNoDoc.json").getFile(),
          Charset.defaultCharset());
  private String deviceRequestNoPrefetchNoDoc = FileUtils
      .readFileToString(new ClassPathResource("requests/deviceRequestNoPrefetchNoDoc.json").getFile(),
          Charset.defaultCharset());

  @LocalServerPort
  private int port;
  @Autowired
  private TestRestTemplate restTemplate;

  private HttpHeaders headers = new HttpHeaders();

  public PrefetchIntegrationTest() throws IOException {
    headers.setContentType(MediaType.APPLICATION_JSON);

  }


  /**
   * Loads the request resources to the EHR FHIR server
   * @param resourceFile - the request with prefetch
   * @param bundleName - name of the bundle in the prefetch we look in
   */
  public void setup(String resourceFile, String bundleName) {

    JsonObject prefetchResources =  new JsonParser().parse(resourceFile).getAsJsonObject();
    JsonArray entries = prefetchResources.get("prefetch")
        .getAsJsonObject().get(bundleName)
        .getAsJsonObject().get("entry").getAsJsonArray();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    // put all resources into the fhir server
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
    // setup with request that has a prefetch so we know all the
    // necessary resources are in the EHR FHIR server
    setup(medicationRequestPrefetch, "medicationRequestBundle");
    HttpEntity<String> entity = new HttpEntity<String>(medicationRequestNoPrefetch, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/medication-prescribe-crd", entity,
            JsonNode.class);
    assert(!cards.get("cards").get(0).get("detail").isNull());
  }

  @Test
  public void testMedicationPrescribeWithPrefetchDocReq() {
    HttpEntity<String> entity = new HttpEntity<String>(medicationRequestPrefetch, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/medication-prescribe-crd", entity,
            JsonNode.class);
    System.out.println(cards);
    assert(!cards.get("cards").get(0).get("detail").isNull());
  }

  @Test
  public void testMedicationPrescribeNoPrefetchNoDocReq() {
    setup(medicationRequestPrefetchNoDoc, "medicationRequestBundle");
    HttpEntity<String> entity = new HttpEntity<String>(medicationRequestNoPrefetchNoDoc, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/medication-prescribe-crd", entity,
            JsonNode.class);
    assert(cards.get("cards").get(0).get("detail").isNull());
  }

  @Test
  public void testMedicationPrescribePrefetchNoDocReq() {
    HttpEntity<String> entity = new HttpEntity<String>(medicationRequestPrefetchNoDoc, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/medication-prescribe-crd", entity,
            JsonNode.class);
    assert(cards.get("cards").get(0).get("detail").isNull());
  }

  @Test
  public void testDeviceRequestNoPrefetchDocReq() {
    setup(deviceRequestPrefetch, "deviceRequestBundle");
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestNoPrefetch, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-review-crd", entity,
            JsonNode.class);
    assert(!cards.get("cards").get(0).get("detail").isNull());
  }

  @Test
  public void testDeviceRequestWithPrefetchDocReq() {
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestPrefetch, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-review-crd", entity,
            JsonNode.class);
    System.out.println(cards);
    assert(!cards.get("cards").get(0).get("detail").isNull());
  }

  @Test
  public void testDeviceRequestPrefetchNoDocReq() {
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestPrefetchNoDoc, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-review-crd", entity,
            JsonNode.class);
    assert(cards.get("cards").get(0).get("detail").isNull());
  }


  @Test
  public void testDeviceRequestNoPrefetchNoDocReq() {
    setup(deviceRequestPrefetchNoDoc, "deviceRequestBundle");
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestNoPrefetchNoDoc, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-review-crd", entity,
            JsonNode.class);
    assert(cards.get("cards").get(0).get("detail").isNull());
  }
}
