package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.hl7.davinci.endpoint.Application;
import org.junit.Ignore;
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
public class EndToEndRequesMultipleCardsTest {

  private String deviceRequestFullPrefetch = FileUtils
      .readFileToString(new ClassPathResource("deviceRequestFullPrefetch_r4.json").getFile(),
          Charset.defaultCharset());

  @LocalServerPort
  private int port;
  @Autowired
  private TestRestTemplate restTemplate;

  public EndToEndRequesMultipleCardsTest() throws IOException {
  }

  @Ignore("No CQL R4 Support at this time.") @Test
  public void shouldReceiveResponseWithTwoCards() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(deviceRequestFullPrefetch, headers);
    JsonNode cards = restTemplate
        .postForObject("http://localhost:" + port + "/r4/cds-services/order-review-crd", entity,
            JsonNode.class);

    assertEquals(2, cards.get("cards").size());
  }

}
