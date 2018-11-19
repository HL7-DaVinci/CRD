package org.hl7.davinci.endpoint.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.database.PublicKey;
import org.hl7.davinci.endpoint.database.PublicKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
public class PublicKeyController {
  private static Logger logger = Logger.getLogger(Application.class.getName());

  private ClassLoader classLoader = getClass().getClassLoader();

  private String keystorePath = Objects.requireNonNull(classLoader
      .getResource("keystore.json"))
      .getFile();

  @Autowired
  private PublicKeyRepository publicKeyRepository;

  /**
   * Gets the json of the keystore.
   * @return a string version of the keystore json file
   */
  @CrossOrigin
  @RequestMapping(value = "/api/public", method = RequestMethod.GET, produces = "application/json")
  public List<PublicKey> getPublicKeys() {
    return publicKeyRepository.findKeys();
  }

  /**
   * Retrieves specific public key using its keyID.
   * @param id the keyID (kid) of a key.
   * @return the public key associated with the id.
   */
  @CrossOrigin
  @GetMapping("/api/public/{id}")
  public PublicKey getPublicKeyById(@PathVariable String id) {
    Optional<PublicKey> henlo = publicKeyRepository.findById(id);
    if (henlo.isPresent()) {
      return henlo.get();
    } else {
      return null;
    }

  }

  /**
   * Allows post requests to modify the keystore.
   * @param jsonData the new keystore data
   * @return the response from the server
   */
  @CrossOrigin
  @PostMapping("/api/public")
  public ResponseEntity<Object> setPublicKeys(@RequestBody String jsonData) {
    Gson gson = new GsonBuilder().create();
    JsonParser parser = new JsonParser();
    JsonObject jwtObject = parser.parse(jsonData).getAsJsonObject();
    String id = jwtObject.get("id").getAsString();
    String key = jwtObject.get("key").getAsString();
    PublicKey publicKey = new PublicKey();
    publicKey.setId(id);
    publicKey.setKey(key);

    publicKeyRepository.save(publicKey);
    return ResponseEntity.noContent().build();
  }

  /**
   * Updates a key already in the repository.
   * @param jsonData the content to replace the key's old content
   * @param id the keyID of the key to be updated
   * @return an empty response
   */
  @CrossOrigin
  @PutMapping("/api/public/{id}")
  public ResponseEntity<Object> changeKey(@RequestBody String jsonData, @PathVariable String id) {
    if (publicKeyRepository.findById(id) != null) {
      publicKeyRepository.deleteById(id);
      JsonParser parser = new JsonParser();
      JsonObject jwtObject = parser.parse(jsonData).getAsJsonObject();
      String newId = jwtObject.get("id").getAsString();
      String key = jwtObject.get("key").getAsString();
      PublicKey publicKey = new PublicKey();
      publicKey.setId(newId);
      publicKey.setKey(key);

      publicKeyRepository.save(publicKey);
    }
    return ResponseEntity.noContent().build();
  }

  /**
   * Deletes specific key.
   * @param id the keyID of the key to be deleted
   * @return an empty response
   */
  @CrossOrigin
  @DeleteMapping("/api/public/{id}")
  public ResponseEntity<Object> deleteKey(@PathVariable String id) {
    publicKeyRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }


}

