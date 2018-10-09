import com.fasterxml.jackson.databind.JsonNode;
import org.hl7.davinci.endpoint.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PublicKeyControllerTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Before
  @Test
  public void testPostKey() {
    JsonNode result = restTemplate.postForObject("http://localhost:" + port + "/api/public",
        "{'id':'sampleId','key':'genericKey'}",JsonNode.class);
    assertNull(result);
  }
  @Test
  public void testtest() {
    JsonNode hea = restTemplate.getForObject(
        "http://localhost:" + port + "/api/public", JsonNode.class);
    assertEquals(hea.get(0).get("key").textValue(),"genericKey");
  }
}
