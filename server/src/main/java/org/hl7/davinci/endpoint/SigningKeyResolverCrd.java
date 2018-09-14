package org.hl7.davinci.endpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SigningKeyResolverCrd extends SigningKeyResolverAdapter {
  @Override
  public PublicKey resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
    String keyId = jwsHeader.getKeyId();

    String jku = (String) jwsHeader.get("jku");
    RestTemplate restTemplate = new RestTemplate();
    // Fetch the public key from the JKU.  Right now
    // only PEM (X509) format is supported.

    String result = restTemplate.getForObject(jku + "/" + keyId, String.class);
    JsonParser parser = new JsonParser();
    JsonObject jwkPub = parser.parse(result).getAsJsonObject();

    try {
      return keyLookup(jwkPub);
    } catch (NoSuchAlgorithmException e) {

      e.printStackTrace();
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
    }

    return null;
  }


  private PublicKey keyLookup(JsonObject jwkPub) throws NoSuchAlgorithmException, InvalidKeySpecException {
    String pem = jwkPub.get("pem").getAsString();
    // Trim the extra bits of the key
    String pubKeyPem = pem.replace("-----BEGIN PUBLIC KEY-----", "");
    pubKeyPem = pubKeyPem.replace("-----END PUBLIC KEY-----", "");
    pubKeyPem = pubKeyPem.replaceAll("(\\r|\\n)", "");

    byte[] encoded = Base64.getDecoder().decode(pubKeyPem);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded);
    KeyFactory factory = KeyFactory.getInstance("RSA");

    return factory.generatePublic(spec);
  }
}
