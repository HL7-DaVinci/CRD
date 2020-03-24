package org.hl7.davinci.endpoint.vsac;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class VSACLoader {

  public static final String AUTH_URL = "https://vsac.nlm.nih.gov/vsac/ws/Ticket";

  private String ticketGrantingTicket;
  private CloseableHttpClient client;

  public VSACLoader(String username, String password) {
    this.client = HttpClients.createDefault();
    HttpClients.custom()
    this.ticketGrantingTicket = getTicketGrantingTicket(username, password);
  }

  private String getTicketGrantingTicket(String username, String password) {
    List<NameValuePair> credentials = new ArrayList<NameValuePair>();
    credentials.add(new BasicNameValuePair("username", username));
    credentials.add(new BasicNameValuePair("password", password));
    System.out.println(" " + username + " " + password);

    HttpPost tgtRequest = new HttpPost(AUTH_URL);
    tgtRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
    try {
      tgtRequest.setEntity(new UrlEncodedFormEntity(credentials, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      // This shouldn't happen.
    }

    String tgt = "";
    try {
      CloseableHttpResponse response = this.client.execute(tgtRequest);
      try {
        HttpEntity responseEntity = response.getEntity();
        tgt = EntityUtils.toString(responseEntity);
      } finally {
        response.close();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } 
    
    return tgt;
  }

  public String getTGT() {
    return this.ticketGrantingTicket;
  }
}