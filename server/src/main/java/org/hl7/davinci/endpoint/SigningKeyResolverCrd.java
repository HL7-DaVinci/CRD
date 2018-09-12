package org.hl7.davinci.endpoint;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;

import java.security.Key;
import java.security.PublicKey;

public class SigningKeyResolverCrd extends SigningKeyResolverAdapter {
  @Override
  public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims){
    String keyId = jwsHeader.getKeyId();
    System.out.println(keyId);
    System.out.println(jwsHeader.getAlgorithm());
    String jku = (String) jwsHeader.get("jku");
    System.out.println(jku);

    return keyLookup(jku);
  }

  private Key keyLookup(String jku) {
    Key key = new PublicKey() {
      @Override
      public String getAlgorithm() {
        return null;
      }

      @Override
      public String getFormat() {
        return null;
      }

      @Override
      public byte[] getEncoded() {
        return new byte[0];
      }
    };
    return key;
  }
}
