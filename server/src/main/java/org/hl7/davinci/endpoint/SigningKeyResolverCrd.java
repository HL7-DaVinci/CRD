package org.hl7.davinci.endpoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SigningKeyResolverCrd extends SigningKeyResolverAdapter {
  @Override
  public PublicKey resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
    String keyId = jwsHeader.getKeyId();

    String jku = (String) jwsHeader.get("jku");
    JsonObject jwkPub = null;
    ClassLoader classLoader = getClass().getClassLoader();
    String keystorePath = classLoader.getResource("keystore.json").getFile();
    System.out.println(keystorePath);
    try (Reader reader = new FileReader(keystorePath)) {
      // check to see if pub key is already in keystore
      Gson gson = new Gson();
      JsonObject keystore = gson.fromJson(reader,JsonObject.class);
      if (keystore.has(keyId)) {
        // if it's already stored, retrieve it
        jwkPub = keystore.get(keyId).getAsJsonObject();
        System.out.println("Retrieved key from keystore");

      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (jwkPub == null) {
      // If the key wasn't loaded from the store, we go
      // find it at the jku.
      RestTemplate restTemplate = new RestTemplate();
      // Fetch the public key from the JKU.  Right now
      // only PEM (X509) format is supported.
      String result = restTemplate.getForObject(jku + "/" + keyId, String.class);
      JsonParser parser = new JsonParser();
      jwkPub = parser.parse(result).getAsJsonObject();
      jwkPub = jwkPub.get("pem").getAsJsonObject();
    }
    try {
      PublicKey returnKey = keyLookup(jwkPub);
      // write the new pub key to the key store
      // store it in the form {keyId:jwk}
      JsonObject pubKeyJson = new JsonObject();
      pubKeyJson.add(keyId,jwkPub);
      try (Writer writer = new FileWriter(keystorePath)) {
        Gson gsonBuilder = new GsonBuilder().create();
        gsonBuilder.toJson(pubKeyJson,writer);
      } catch (IOException e) {
        e.printStackTrace();
      }
      return returnKey;
    } catch (NoSuchAlgorithmException e) {

      e.printStackTrace();
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
    }

    return null;
  }


  private PublicKey keyLookup(JsonObject jwkPub) throws NoSuchAlgorithmException, InvalidKeySpecException {


//    byte[] encoded = Base64.getDecoder().decode(pem.get("n").getAsString());
    BigInteger m = new BigInteger(1,Base64.getUrlDecoder().decode(jwkPub.get("n").getAsString()));
    BigInteger e = new BigInteger(1,Base64.getUrlDecoder().decode(jwkPub.get("e").getAsString()));
    RSAPublicKeySpec spec = new RSAPublicKeySpec(m,e);



    // Trim the extra bits of the key
//    String pubKeyPem = pem.replace("-----BEGIN PUBLIC KEY-----", "");
//    pubKeyPem = pubKeyPem.replace("-----END PUBLIC KEY-----", "");
//    pubKeyPem = pubKeyPem.replaceAll("(\\r|\\n)", "");
//
//    byte[] encoded = Base64.getDecoder().decode(pubKeyPem);
//    X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded);
    KeyFactory factory = KeyFactory.getInstance("RSA");


    return factory.generatePublic(spec);
  }
}
