package org.hl7.davinci.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

public class SigningKeyResolverCrd extends SigningKeyResolverAdapter {
  private static Logger logger = Logger.getLogger(Application.class.getName());

  @Override
  public PublicKey resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
    String keyId = jwsHeader.getKeyId();

    String jku = (String) jwsHeader.get("jku");
    JsonObject jwkPub = null;

    RestTemplate restTemplate = new RestTemplate();

    // TODO: update this url if the location of /public changes.
    String keyUrl = "http://localhost:8090/api/public/";
    String jwkString = null;
    try {
      ResponseEntity<org.hl7.davinci.endpoint.database.PublicKey> response
          = restTemplate.getForEntity(keyUrl + keyId, org.hl7.davinci.endpoint.database.PublicKey.class);
      if (response.getBody() != null) {
        jwkString = response.getBody().getKey();
        jwkPub = new JsonParser().parse(jwkString).getAsJsonObject();
        logger.info("Public Key found in keystore");
      } else {
        logger.info("Public Key not found in keystore");
      }
    } catch (HttpServerErrorException e) {
      logger.warning("Public Key not retrieved");
    }
    if (jwkPub == null) {
      // If the key wasn't loaded from the store, we go
      // find it at the jku.
      logger.info("Retrieving public key from " + jku);
      // Fetch the public key from the JKU.  Right now
      // only PEM (X509) format is supported.
      String result = restTemplate.getForObject(jku + "/" + keyId, String.class);
      JsonParser parser = new JsonParser();
      jwkPub = parser.parse(result).getAsJsonObject();
      // "pem" is just the arbitrary key used in the json
      // that gets built in "request-builder"
      jwkString = jwkPub.get("pem").toString();
      jwkPub = jwkPub.get("pem").getAsJsonObject();
      org.hl7.davinci.endpoint.database.PublicKey payload =
          new org.hl7.davinci.endpoint.database.PublicKey();
      payload.setId(keyId);
      payload.setKey(jwkString);
      try {
        ResponseEntity<String> response
            = restTemplate.postForEntity(keyUrl,payload, String.class);
        logger.info("Saved public key to keystore");

      } catch (HttpServerErrorException e) {
        logger.warning("Key was not saved");
      }
    }
    try {
      // write the new pub key to the key store
      // store it in the form {keyId:jwk}
      return keyLookup(jwkPub);
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
