package org.hl7.davinci.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.logging.Logger;

public class SigningKeyResolverCrd extends SigningKeyResolverAdapter {
  private static Logger logger = Logger.getLogger(Application.class.getName());

  @Override
  public PublicKey resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
    String keyId = jwsHeader.getKeyId();

    String jku = (String) jwsHeader.get("jku");
    JsonObject jwkPub = null;
    ClassLoader classLoader = getClass().getClassLoader();
    String keystorePath = Objects.requireNonNull(classLoader
        .getResource("keystore.json"))
        .getFile();
    JsonObject keystore = null;
    try (Reader reader = new FileReader(keystorePath)) {
      // check to see if pub key is already in keystore
      Gson gson = new Gson();
      keystore = gson.fromJson(reader,JsonObject.class);
      if (keystore.has(keyId)) {
        // if it's already stored, retrieve it
        jwkPub = keystore.get(keyId).getAsJsonObject();
        logger.info("Retrieved key from keystore");

      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (jwkPub == null) {
      // If the key wasn't loaded from the store, we go
      // find it at the jku.
      logger.info("Retrieving public key from " + jku);
      RestTemplate restTemplate = new RestTemplate();
      // Fetch the public key from the JKU.  Right now
      // only PEM (X509) format is supported.
      String result = restTemplate.getForObject(jku + "/" + keyId, String.class);
      JsonParser parser = new JsonParser();
      jwkPub = parser.parse(result).getAsJsonObject();
      // "pem" is just the arbitrary key used in the json
      // that gets built in "request-builder"
      jwkPub = jwkPub.get("pem").getAsJsonObject();
    }
    try {
      final PublicKey returnKey = keyLookup(jwkPub);
      // write the new pub key to the key store
      // store it in the form {keyId:jwk}

      try (Writer writer = new FileWriter(keystorePath)) {

        if (keystore == null) {
          keystore = new JsonObject();
        }
        System.out.println(keystore.keySet());
        keystore.add(keyId,jwkPub);
        Gson gsonBuilder = new GsonBuilder().create();
        gsonBuilder.toJson(keystore,writer);
      } catch (IOException e) {
        e.printStackTrace();
      }
      logger.info("Saved public key to keystore");
      return returnKey;
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      e.printStackTrace();
    }

    return null;
  }


  private PublicKey keyLookup(JsonObject jwkPub) throws NoSuchAlgorithmException, InvalidKeySpecException {
    // The modulus and exponent in the JWK are base64 encoded.  The bits of the
    // mod and exp are signed.  The extra bit is taken care of by having the
    // signum set to 1 (positive).
    BigInteger modulus = new BigInteger(1,Base64.getUrlDecoder().decode(jwkPub.get("n").getAsString()));
    BigInteger exponent = new BigInteger(1,Base64.getUrlDecoder().decode(jwkPub.get("e").getAsString()));
    RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus,exponent);

    KeyFactory factory = KeyFactory.getInstance("RSA");


    return factory.generatePublic(spec);
  }
}
