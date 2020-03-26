package org.hl7.davinci.endpoint.vsac;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.util.IO;
import org.hl7.davinci.endpoint.vsac.errors.VSACException;
import org.hl7.davinci.endpoint.vsac.errors.VSACInvalidCredentialsException;

public class VSACLoader {

  public static final String AUTH_URL = "https://vsac.nlm.nih.gov/vsac/ws/Ticket";
  public static final String SVS_URL = "https://vsac.nlm.nih.gov/vsac/svs/RetrieveMultipleValueSets";
  public static final String DEFAULT_PROFILE = "Most Recent Code System Versions in VSAC";

  private String ticketGrantingTicket;
  private CloseableHttpClient client;

  public VSACLoader(String username, String password) throws VSACException {
    this.client = HttpClients.createDefault();
    this.ticketGrantingTicket = getTicketGrantingTicket(username, password);
  }

  private String getTicketGrantingTicket(String username, String password) throws VSACException {
    List<NameValuePair> credentials = new ArrayList<NameValuePair>();
    credentials.add(new BasicNameValuePair("username", username));
    credentials.add(new BasicNameValuePair("password", password));

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
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 401) {
          throw new VSACInvalidCredentialsException();
        } else if (statusCode == 200) {
          tgt = EntityUtils.toString(responseEntity);
        } else {
          throw new VSACException("Unexpected response in getting ticket granting ticket. Status code: " + statusCode);
        }
      } finally {
        response.close();
      }
    } catch (IOException ioe) {
      throw new VSACException("Unexpected error in getting ticket granting ticket.", ioe);
    }

    return tgt;
  }

  public String getTicket() throws VSACException {
    // Service info that needs to be passed when getting a ticket.
    List<NameValuePair> serviceInfo = new ArrayList<NameValuePair>();
    serviceInfo.add(new BasicNameValuePair("service", "http://umlsks.nlm.nih.gov"));

    // Auth url with the ticket granting ticket added to it is the url to hit.
    HttpPost ticketRequest = new HttpPost(AUTH_URL + '/' + this.ticketGrantingTicket);
    ticketRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
    try {
      ticketRequest.setEntity(new UrlEncodedFormEntity(serviceInfo, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      // This shouldn't happen.
    }

    String ticket = "";
    try {
      CloseableHttpResponse response = this.client.execute(ticketRequest);
      try {
        HttpEntity responseEntity = response.getEntity();
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 401) {
          // TODO: expired ticket
          throw new VSACInvalidCredentialsException();
        } else if (statusCode == 200) {
          ticket = EntityUtils.toString(responseEntity);
        } else {
          throw new VSACException("Unexpected response in getting service ticket. Status code: " + statusCode);
        }
      } finally {
        response.close();
      }
    } catch (IOException ioe) {
      throw new VSACException("Unexpected error in getting service ticket.", ioe);
    }

    return ticket;
  }

  public String getValueSet(String oid) throws VSACException {
    HttpGet vsRequest;
    try {
      URIBuilder vsUriBuilder;
      vsUriBuilder = new URIBuilder(SVS_URL);    
      vsUriBuilder.setParameter("ticket", this.getTicket())
        .setParameter("id", oid)
        .setParameter("profile", DEFAULT_PROFILE)
        .setParameter("includeDraft", "yes");

      vsRequest = new HttpGet(vsUriBuilder.build());
    } catch (URISyntaxException e) {
      throw new VSACException("Unable to build URI for fetching valueset.", e);
    }
    
    String valueSet = "";
    try {
      CloseableHttpResponse response = this.client.execute(vsRequest);
      try {
        HttpEntity responseEntity = response.getEntity();
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 401) {
          // TODO: expired ticket
          throw new VSACInvalidCredentialsException();
        } else if (statusCode == 200) {
          valueSet = EntityUtils.toString(responseEntity);
        } else {      
          // TODO: Handle 404s 
          throw new VSACException("Unexpected response in getting value set. Status code: " + statusCode);
        }
      } finally {
        response.close();
      }
    } catch (IOException ioe) {
      throw new VSACException("Unexpected error in getting valueset.", ioe);
    } 
    
    return valueSet;
  }

  public String getTGT() {
    return this.ticketGrantingTicket;
  }

  public void close() throws VSACException {
    try {
      this.client.close();
    } catch(IOException ioe) {
      throw new VSACException("Error closing HTTP Client.", ioe);
    }
  }
}