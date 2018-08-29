package org.hl7.davinci.endpoint.cdshooks.services.crd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Calendar;

import org.hl7.davinci.CrdRequestCreator;
import org.hl7.davinci.cdshooks.CdsResponse;
import org.hl7.davinci.cdshooks.orderreview.OrderReviewRequest;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderReviewServiceTest {

  private static String requestBlob = "{\n"
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
      + "                  \"code\": \"E0424\"\n"
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
      + "  \"prefetch\": {\n"
      + "    \"resourceType\": \"Bundle\",\n"
      + "    \"id\": \"f452a78a-da06-4fe6-8233-ad2a817c96\",\n"
      + "    \"meta\": {\n"
      + "      \"lastUpdated\": \"2018-08-29T16:07:48Z\"\n"
      + "    },\n"
      + "    \"type\": \"searchset\",\n"
      + "    \"total\": 1,\n"
      + "    \"link\": [\n"
      + "      {\n"
      + "        \"relation\": \"self\",\n"
      + "        \"url\": \"http://localhost:8080/DeviceRequest/DeviceRequest?id=24439&_include=DeviceRequest:patient\"\n"
      + "      }\n"
      + "    ],\n"
      + "    \"entry\": [\n"
      + "      {\n"
      + "        \"fullUrl\": \"http://localhost:8080/DeviceRequest/24439\",\n"
      + "        \"resource\": {\n"
      + "          \"resourceType\": \"DeviceRequest\",\n"
      + "          \"id\": \"24439\",\n"
      + "          \"status\": \"draft\",\n"
      + "          \"intent\": \"plan\",\n"
      + "          \"codeCodeableConcept\": {\n"
      + "            \"coding\": [\n"
      + "              {\n"
      + "                \"system\": \"https://bluebutton.cms.gov/resources/codesystem/hcpcs\",\n"
      + "                \"code\": \"E0424\"\n"
      + "              }\n"
      + "            ],\n"
      + "            \"text\": \"Stationary Compressed Gaseous Oxygen System, Rental\"\n"
      + "          },\n"
      + "          \"subject\": {\n"
      + "            \"reference\": \"Patient/1288992\"\n"
      + "          },\n"
      + "          \"authoredOn\": \"2018-08-08\"\n"
      + "        },\n"
      + "        \"search\": {\n"
      + "          \"mode\": \"match\"\n"
      + "        }\n"
      + "      },\n"
      + "      {\n"
      + "        \"fullUrl\": \"http://localhost:8080/Patient/1288992\",\n"
      + "        \"resource\": {\n"
      + "          \"resourceType\": \"Patient\",\n"
      + "          \"id\": \"1288992\",\n"
      + "          \"gender\": \"male\",\n"
      + "          \"birthDate\": \"1970-07-04\"\n"
      + "        },\n"
      + "        \"search\": {\n"
      + "          \"mode\": \"include\"\n"
      + "        }\n"
      + "      }\n"
      + "    ]\n"
      + "  }\n"
      + "}";

  @Test
  public void testHandleRequest1111() {



    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    OrderReviewRequest request = CrdRequestCreator.createRequest(Enumerations.AdministrativeGender.MALE, cal.getTime());
    OrderReviewService service = new OrderReviewService();
    CdsResponse response = service.handleRequest(request);

    assertNotNull(response);
    assertEquals(1, response.getCards().size());
    assertEquals("empty card", response.getCards().get(0).getSummary());
  }


  @Autowired
  private OrderReviewService service;

  @Test
  public void testHandleRequest() {
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    OrderReviewRequest request = CrdRequestCreator.createRequest(Enumerations.AdministrativeGender.MALE, cal.getTime());
    CdsResponse response = service.handleRequest(request);
    assertNotNull(response);
    assertEquals(1, response.getCards().size());
    assertEquals("No documentation rules found", response.getCards().get(0).getSummary());
  }
}
