package org.hl7.davinci.endpoint.cdshooks.services.crd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import org.hl7.davinci.endpoint.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class ServicesDiscoveryEndpointTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void shouldReturnServiceListR4() {
    JsonNode r4CdsServiceInformation = restTemplate.getForObject(
        "http://localhost:" + port + "/r4/cds-services/", JsonNode.class);
    JsonNode r4OrderReviewCrd = r4CdsServiceInformation.get("services").get(0);
    JsonNode r4MedicationPrescribeCrd = r4CdsServiceInformation.get("services").get(1);

    assertEquals(r4OrderReviewCrd.get("id").textValue(), "order-review-crd");
    assertEquals(r4OrderReviewCrd.get("hook").textValue(), "order-review");

    assertEquals(r4MedicationPrescribeCrd.get("id").textValue(), "medication-prescribe-crd");
    assertEquals(r4MedicationPrescribeCrd.get("hook").textValue(), "medication-prescribe");
  }

  @Test
  public void shouldReturnServiceListStu3() {
    JsonNode stu3CdsServiceInformation = restTemplate.getForObject(
        "http://localhost:" + port + "/stu3/cds-services/", JsonNode.class);
    JsonNode stu3OrderReviewCrd = stu3CdsServiceInformation.get("services").get(0);
    JsonNode stu3MedicationPrescribeCrd = stu3CdsServiceInformation.get("services").get(1);

    assertEquals(stu3OrderReviewCrd.get("id").textValue(), "order-review-crd");
    assertEquals(stu3OrderReviewCrd.get("hook").textValue(), "order-review");

    assertEquals(stu3MedicationPrescribeCrd.get("id").textValue(), "medication-prescribe-crd");
    assertEquals(stu3MedicationPrescribeCrd.get("hook").textValue(), "medication-prescribe");
  }
}


