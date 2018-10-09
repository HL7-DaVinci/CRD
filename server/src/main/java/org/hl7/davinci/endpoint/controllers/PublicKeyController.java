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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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
  PublicKeyRepository publicKeyRepository;

  /**
   * Gets the json of the keystore.
   * @return a string version of the keystore json file
   */
  @CrossOrigin
  @RequestMapping(value = "/api/public", method = RequestMethod.GET, produces = "application/json")
  public List<PublicKey> getPublicKeys() {

//    JsonObject keystore = null;
//    try (Reader reader = new FileReader(keystorePath)) {
//      // check to see if pub key is already in keystore
//      Gson gson = new Gson();
//
//      keystore = gson.fromJson(reader,JsonObject.class);
//      logger.info("keystore requested");
//    } catch (IOException e) {
//      e.printStackTrace();
//    }

    return publicKeyRepository.findKeys();
  }

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
    PublicKey pKey = new PublicKey();
    pKey.setId(id);
    pKey.setKey(key);

    publicKeyRepository.save(pKey);
    return ResponseEntity.noContent().build();
  }

  @CrossOrigin
  @PutMapping("/api/public/{id}")
  public ResponseEntity<Object> changeKey(@RequestBody String jsonData, @PathVariable String id) {
    if (publicKeyRepository.findById(id) != null) {
      publicKeyRepository.deleteById(id);
      Gson gson = new GsonBuilder().create();
      JsonParser parser = new JsonParser();
      JsonObject jwtObject = parser.parse(jsonData).getAsJsonObject();
      String newId = jwtObject.get("id").getAsString();
      String key = jwtObject.get("key").getAsString();
//    try (Writer writer = new FileWriter(keystorePath)) {
//      gson.toJson(jwtObject, writer);
//      logger.info("saved keystore");
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
      PublicKey pKey = new PublicKey();
      pKey.setId(newId);
      pKey.setKey(key);

      publicKeyRepository.save(pKey);

    }
    return ResponseEntity.noContent().build();

  }

  @CrossOrigin
  @DeleteMapping("/api/public/{id}")
  public ResponseEntity<Object> deleteKey(@PathVariable String id) {
    publicKeyRepository.deleteById(id);
    return ResponseEntity.noContent().build();

  }


}

