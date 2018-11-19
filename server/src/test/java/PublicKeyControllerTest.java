import com.fasterxml.jackson.databind.JsonNode;
import org.hl7.davinci.endpoint.Application;

import org.hl7.davinci.endpoint.database.PublicKey;
import org.hl7.davinci.endpoint.database.PublicKeyRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PublicKeyControllerTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private PublicKeyRepository publicKeyRepository;

  @Before
  public void setup() {
    PublicKey key = new PublicKey();
    key.setId("sampleId");
    key.setKey("genericKey");
    publicKeyRepository.save(key);
    PublicKey key2 = new PublicKey();
    key2.setId("ibImKzMh5F5fzLCT1r4kUcHT5gmWyPZ4atErhZ697KM");
    key2.setKey("{\"kty\":\"RSA\",\"n\":\"8C2EIZnopqAjqRV_HPHyESYt3GtzklES411_LpAT0pzufPAiaJY9jSk_isVenOzaGR3scVdNfqc06uV2ouoJLpoZ-1qovrzRoz3ZmosXyNF67OqUK7XQGVTPDX2jALW1Kmco5RiBvE8sQniOvLgUdkpgUbKmrnXpcjia36n_GT9XMmusfhvWsLAV6M6fN5vNHASRkyGwNN3zwW46a6gmSYpnsqtOzI_ydE_fCgldmdFz6m95wJMWHQTMyIvvF1ZaogqSiyUvYspleMRaOLNMN0S2r_arPE5NFWA2-WjlMyWQ7i-RZ-Zht4I8YDr4U72ofjg8gSgK9FAKYLqIwefzGQ\",\"e\":\"AQAB\"}");
    publicKeyRepository.save(key2);
  }

  @Test
  public void testPostKey() {
    JsonNode result = restTemplate.postForObject("http://localhost:" + port + "/api/public",
        "{'id':'testPost','key':'genericPostKey'}",JsonNode.class);
    assertEquals(publicKeyRepository.findById("testPost")
        .get()
        .getKey(),"genericPostKey");
  }
  @Test
  public void testGetKeys() {

    JsonNode hea = restTemplate.getForObject(
        "http://localhost:" + port + "/api/public", JsonNode.class);
    assertEquals(hea.get(0).get("key").textValue(),"genericKey");
  }

  @Test
  public void testDeleteKey() {
    restTemplate.delete(
        "http://localhost:" + port + "/api/public/ibImKzMh5F5fzLCT1r4kUcHT5gmWyPZ4atErhZ697KM");
    assertFalse(publicKeyRepository
        .findById("ibImKzMh5F5fzLCT1r4kUcHT5gmWyPZ4atErhZ697KM")
        .isPresent());
  }
}
