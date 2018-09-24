package org.hl7.davinci.ehrServer;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class ClientAuthorizationInterceptor extends AuthorizationInterceptor {

  String introspectUrl = "http://localhost:8180/auth/realms/"
      + Config.get("realm") + "/protocol/openid-connect/token/introspect";

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
    String use_oauth = Config.get("use_oauth");
    if (!Boolean.parseBoolean(use_oauth)) {
      return new RuleBuilder()
          .allowAll()
          .build();
    }
    CloseableHttpClient client = HttpClients.createDefault();

    String authHeader = theRequestDetails.getHeader("Authorization");
    // Get the token and drop the "Bearer"
    if (authHeader == null) {
      return new RuleBuilder()
          .denyAll("No authorization header present")
          .build();
    }
    String token = authHeader.split(" ")[1];

    String secret = Config.get("client_secret");
    String clientId = Config.get("client_id");

    HttpPost httpPost = new HttpPost(introspectUrl);
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("client_id", clientId));
    params.add(new BasicNameValuePair("client_secret", secret));
    params.add(new BasicNameValuePair("token", token));
    try {
      httpPost.setEntity(new UrlEncodedFormEntity(params));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    JsonObject jsonResponse;
    try {
      CloseableHttpResponse response = client.execute(httpPost);
      String jsonString = EntityUtils.toString(response.getEntity());
      jsonResponse = new JsonParser().parse(jsonString).getAsJsonObject();
      client.close();

    } catch (IOException e) {
      e.printStackTrace();
      jsonResponse = null;
    }

    if (jsonResponse.get("active").getAsBoolean()) {
      return new RuleBuilder()
          .allowAll()
          .build();
    } else {
      return new RuleBuilder()
          .denyAll("Rejected OAuth token - failed to introspect")
          .build();
    }
  }


}