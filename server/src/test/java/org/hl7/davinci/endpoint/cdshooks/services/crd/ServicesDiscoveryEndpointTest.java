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
    JsonNode r4OrderSignCrd = r4CdsServiceInformation.get("services").get(0);


    assertEquals(r4OrderSignCrd.get("id").textValue(), "order-sign-crd");
    assertEquals(r4OrderSignCrd.get("hook").textValue(), "order-sign");
  }
}


