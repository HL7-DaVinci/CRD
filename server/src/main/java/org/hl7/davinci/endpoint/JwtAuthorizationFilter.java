package org.hl7.davinci.endpoint;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Jwts;
import org.hl7.davinci.endpoint.database.RequestLog;
import org.hl7.davinci.endpoint.database.RequestService;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;


public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

  RequestService requestService;

  public JwtAuthorizationFilter(AuthenticationManager authManager, RequestService requestService) {
    super(authManager);
    this.requestService = requestService;
  }



  @Override
  protected void doFilterInternal(HttpServletRequest req,
                                  HttpServletResponse res,
                                  FilterChain chain) throws IOException, ServletException {

    String requestStr;
    try {
      ObjectMapper mapper = new ObjectMapper();
      ObjectWriter w = mapper.writer();
      requestStr = w.writeValueAsString(req);
    } catch (Exception e) {
      logger.error("failed to write request json: " + e.getMessage());
      requestStr = "{\"error\": \"Authorization failed, request rejected\"}";
    }

    RequestLog requestLog = new RequestLog(requestStr.getBytes(), new Date().getTime());


    String header = req.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer")) {
      requestService.create(requestLog);
      logger.warn("JWT authorization failed - no bearer auth token present");
      chain.doFilter(req, res);
      return;
    }
    logger.info("Bearer auth token recieved");
    UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    if (authentication == null) {
      requestService.create(requestLog);
    }
    chain.doFilter(req, res);
  }

  private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
    String token = request.getHeader("Authorization");
    // parse the token.
    token = token.replace("Bearer ", "");
    String user = isTokenWellFormed(token);

    // Check the token's signature.  Throws an exception if the token is rejected
    try {
      // The KeyResolver fetches the public key from the jku
      // will throw an exception if the signature cannot be verified
      Jwts.parser()
          .setSigningKeyResolver(new SigningKeyResolverCrd())
          .parseClaimsJws(token).getSignature();
    } catch (io.jsonwebtoken.SignatureException sigEx) {
      logger.info("Failed to verify token signature, rejecting token.");
      return null;
    }

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
      logger.warn("Token doesn't have proper format, rejecting");
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
        if (!validHeader) {
          requiredHeaders.removeAll(jsonHeader.keySet());
          logger.warn("Token header rejected, missing required properties - " + requiredHeaders);
        }
        if (!validPayload) {
          requiredPayload.removeAll(jsonPayload.keySet());
          logger.warn("Token payload rejected, missing required properties - " + requiredPayload);
        }
        return null;
      }
      // if any part of the above fails, the token
      // is of invalid format
    } catch (IllegalArgumentException decodingError) {
      logger.warn("The token was not properly base64 encoded - cannot parse token");
      return null;

    } catch (IllegalStateException parsingError) {
      logger.warn("The token contained invalid JSON");
      return null;
    }
  }
}