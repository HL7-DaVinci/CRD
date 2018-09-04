import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.hl7.davinci.endpoint.Application;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class EndToEndRequestPrefetchTest {

  private static String requestBlob = "{\n"
      + "  \"hook\": \"order-review\",\n"
      + "  \"hookInstance\": \"a4ca4d19-ed2a-491d-87d4-408fefd56ec9\",\n"
      + "  \"fhirServer\": \"http://localhost:9089/\",\n"
      + "  \"oauth\": null,\n"
      + "  \"user\": \"Practitioner/1234\",\n"
      + "  \"context\":{\n"
      + "    \"patientId\":\"1288992\",\n"
      + "    \"encounterId\":\"89284\",\n"
      + "    \"orders\":{\n"
      + "      \"resourceType\":\"Bundle\",\n"
      + "      \"entry\":[\n"
      + "        {\n"
      + "          \"resource\": {\n"
      + "            \"resourceType\": \"DeviceRequest\",\n"
      + "            \"id\": \"24439\",\n"
      + "            \"status\": \"draft\",\n"
      + "            \"intent\": \"plan\",\n"
      + "            \"codeCodeableConcept\": {\n"
      + "              \"coding\": [\n"
      + "                {\n"
      + "                  \"system\": \"https://bluebutton.cms.gov/resources/codesystem/hcpcs\",\n"
      + "                  \"code\": \"E0250\"\n"
      + "                }\n"
      + "              ],\n"
      + "              \"text\": \"Stationary Compressed Gaseous Oxygen System, Rental\"\n"
      + "            },\n"
      + "            \"subject\": {\n"
      + "              \"reference\": \"Patient/1288992\"\n"
      + "            },\n"
      + "            \"authoredOn\": \"2018-08-08\",\n"
      + "            \"insurance\": {\n"
      + "              \"reference\": \"Coverage/1234\"\n"
      + "            },\n"
      + "            \"performer\": {\n"
      + "              \"reference\": \"PractitionerRole/1234\"\n"
      + "            }\n"
      + "          }\n"
      + "        }\n"
      + "      ]\n"
      + "    }\n"
      + "  },\n"
      + "  \"prefetch\": {}\n"
      + "}";

  private String prefetchResponseBlob = "{\n"
      + "      \"resourceType\": \"Bundle\",\n"
      + "      \"id\": \"f452a78a-da06-4fe6-8233-ad2a817c96\",\n"
      + "      \"meta\": {\n"
      + "        \"lastUpdated\": \"2018-08-29T16:07:48Z\"\n"
      + "      },\n"
      + "      \"type\": \"searchset\",\n"
      + "      \"total\": 1,\n"
      + "      \"link\": [\n"
      + "        {\n"
      + "          \"relation\": \"self\",\n"
      + "          \"url\": \"http://localhost:8080/DeviceRequest/DeviceRequest?id=24439&_include=DeviceRequest:patient\"\n"
      + "        }\n"
      + "      ],\n"
      + "      \"entry\": [\n"
      + "        {\n"
      + "          \"fullUrl\": \"http://localhost:8080/DeviceRequest/24439\",\n"
      + "          \"resource\": {\n"
      + "            \"resourceType\": \"DeviceRequest\",\n"
      + "            \"id\": \"24439\",\n"
      + "            \"status\": \"draft\",\n"
      + "            \"intent\": \"plan\",\n"
      + "            \"codeCodeableConcept\": {\n"
      + "              \"coding\": [\n"
      + "                {\n"
      + "                  \"system\": \"https://bluebutton.cms.gov/resources/codesystem/hcpcs\",\n"
      + "                  \"code\": \"E0424\"\n"
      + "                }\n"
      + "              ],\n"
      + "              \"text\": \"Stationary Compressed Gaseous Oxygen System, Rental\"\n"
      + "            },\n"
      + "            \"subject\": {\n"
      + "              \"reference\": \"Patient/1288992\"\n"
      + "            },\n"
      + "            \"authoredOn\": \"2018-08-08\"\n"
      + "          },\n"
      + "          \"search\": {\n"
      + "            \"mode\": \"match\"\n"
      + "          }\n"
      + "        },\n"
      + "        {\n"
      + "          \"fullUrl\": \"http://localhost:8080/Patient/1288992\",\n"
      + "          \"resource\": {\n"
      + "            \"resourceType\": \"Patient\",\n"
      + "            \"id\": \"1288992\",\n"
      + "            \"gender\": \"male\",\n"
      + "            \"birthDate\": \"1970-07-04\"\n"
      + "          },\n"
      + "          \"search\": {\n"
      + "            \"mode\": \"include\"\n"
      + "          }\n"
      + "        }\n"
      + "      ]\n"
      + "    }";

  private String prefetchUrl = "/DeviceRequest?id=24439&_include=DeviceRequest:patient&_include=DeviceRequest:performer&_include=DeviceRequest:requester&_include=DeviceRequest:device&_include=PractitionerRole:organization&_include=PractitionerRole:practitioner&_include=DeviceRequest:insurance:Coverage";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(9089); // No-args constructor defaults to port 8080

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void shouldSuccessfullyFillPreFetch() {
    stubFor(get(urlEqualTo(prefetchUrl))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(prefetchResponseBlob)));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(requestBlob,headers);
    JsonNode cards = restTemplate.postForObject("http://localhost:" + port + "/cds-services/order-review-crd", entity, JsonNode.class);

    System.out.println(cards);
    assertEquals(cards.get("cards").get(0).get("summary").textValue(), "No documentation rules found");
  }

  @Test
  public void shouldFailToFillPrefetch() {
    stubFor(get(urlEqualTo(prefetchUrl))
        .willReturn(aResponse()
            .withStatus(404)));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(requestBlob,headers);
    JsonNode cards = restTemplate.postForObject("http://localhost:" + port + "/cds-services/order-review-crd", entity, JsonNode.class);

    System.out.println(cards);
    assertEquals(cards.get("cards").get(0).get("summary").textValue(), "DeviceRequestBundle could not be (pre)fetched in this request ");
  }
}


