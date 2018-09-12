package org.hl7.davinci.endpoint;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Arrays;
import java.util.Base64;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
  public JWTAuthorizationFilter(AuthenticationManager authManager) {
    super(authManager);
  }
  @Override
  protected void doFilterInternal(HttpServletRequest req,
                                  HttpServletResponse res,
                                  FilterChain chain) throws IOException, ServletException {

    String header = req.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer")) {
      logger.warn("JWT authorization failed - no bearer auth token present");
      chain.doFilter(req, res);
      return;
    }
    logger.info("Bearer auth token recieved");
    UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    chain.doFilter(req, res);
  }
  private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
    String token = request.getHeader("Authorization");
    // parse the token.
    token = token.replace("Bearer ", "");
    String user = isTokenWellFormed(token);

    // ignore this for now, this will be how the key signature gets validated
    // String user = Jwts.parser().setSigningKeyResolver(new SigningKeyResolverCrd()).parseClaimsJws(token).getHeader().getAlgorithm();

    if (user != null) {
      logger.info("Validated JWT token structure from " + user);
      return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
    }
    logger.warn("Invalid Bearer token - the token was not well formed.");
    return null;
  }

  /**
   * Checks if the token is well formed.  This includes following
   * rfc7515 standard for JWT and the CDS-Hooks specification.
   *
   * @param token the JWT to be checked
   * @return a boolean indicating whether the token is well formed
   */
  private String isTokenWellFormed(String token) {
    JsonParser parser = new JsonParser();
    // JWT tokens are of the form:
    //    header     payload     signature
    //   xxxxxxxxx.yyyyyyyyyyy.zzzzzzzzzzzz
    // where each section is base64 encoded JSON.
    // We need to check if the header and the payload
    // have the correct keys and if the signature is present.
    // The signature will be validated later.

    String[] tokens = token.split("\\.");
    if (tokens.length != 3) {
      // tokens without all three sections are invalid
      return null;
    }
    try {
      String header = new String(Base64.getDecoder().decode(tokens[0]));
      String payload = new String(Base64.getDecoder().decode(tokens[1]));

      // we just want to make sure the json has the correct fields
      // their contents aren't really important, if the contents are
      // bad we can reject them later.
      List<String> requiredHeaders = Arrays.asList("alg","typ","kid");
      List<String> requiredPayload = Arrays.asList("iss","aud","exp","iat","jti");

      JsonObject jsonHeader = parser
          .parse(header)
          .getAsJsonObject();
      boolean validHeader = jsonHeader
          .keySet()
          .containsAll(requiredHeaders);

      JsonObject jsonPayload = parser
          .parse(payload)
          .getAsJsonObject();
      boolean validPayload = jsonPayload
          .keySet()
          .containsAll(requiredPayload);

      if (validHeader && validPayload) {
        return jsonPayload.get("iss").toString();
      } else {
        return null;
      }

    } catch (Exception e) {
      // if any part of the above fails, the token
      // is of invalid format
      return null;
    }
  }
}