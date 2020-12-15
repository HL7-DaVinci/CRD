package org.hl7.davinci.endpoint.vsac;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hl7.davinci.endpoint.vsac.errors.VSACException;
import org.hl7.davinci.endpoint.vsac.errors.VSACInvalidCredentialsException;
import org.hl7.davinci.endpoint.vsac.errors.VSACValueSetNotFoundException;
import org.hl7.fhir.r4.model.ValueSet;
import org.xml.sax.SAXException;

/**
 * Class that handles interaction with the VSAC SVS API.
 */
public class VSACLoader {

  public static final String AUTH_URL = "https://vsac.nlm.nih.gov/vsac/ws/Ticket";
  public static final String SVS_URL = "https://vsac.nlm.nih.gov/vsac/svs/RetrieveMultipleValueSets";
  public static final String DEFAULT_PROFILE = "Most Recent Code System Versions in VSAC";

  /** 
   * The Ticket Granting Ticket that allows us to get service Tickets.
   */
  private String ticketGrantingTicket;

  /**
   * The Http client we will attempt to reuse for all requests.
   */
  private CloseableHttpClient client;

  /**
   * Initializes a VSACLoader. This attmepts to get a VSAC TGT.
   * 
   * @param apiKey UMLS/VSAC API KEY
   * @throws VSACException If there was an inability to get a TGT with these credentials.
   */
  public VSACLoader(String apiKey) throws VSACException {
    this.client = HttpClients.createDefault();
    this.ticketGrantingTicket = getTicketGrantingTicket(apiKey);
  }

  /**
   * Grabs a Ticket Granting Ticket. This is the first step in using the API. This ticket is used to get service tickets which are
   * needed for each request. This ticket /should/ last 8 hours. But in practice it does not.
   *
   * @param apiKey UMLS/VSAC API KEY
   * @return The Ticket Granting Ticket
   * @throws VSACException If there was an issue getting the TGT.
   */
  private String getTicketGrantingTicket(String apiKey) throws VSACException {
    // Build parameter pair for form urlencoded data that will be posted.
    List<NameValuePair> credentials = new ArrayList<NameValuePair>();
    credentials.add(new BasicNameValuePair("apikey", apiKey));

    // Build request to authorization url.
    HttpPost tgtRequest = new HttpPost(AUTH_URL);
    tgtRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
    try {
      tgtRequest.setEntity(new UrlEncodedFormEntity(credentials, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      // This shouldn't happen.
    }

    // Handle request and parsing into string.
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

  /**
   * Get a one time use service ticket. The result of this will be used for a single value set fetch call.
   * 
   * @return The service ticket.
   * @throws VSACException If there was an issue getting the service ticket.
   */
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
          // This usually means the ticket granting ticket has expired.
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

  /**
   * Parse the ValueSet response using a SAXParser.
   * 
   * @param response The InputStream of the response.
   * @return The parsed FHIR R4 ValueSet.
   * @throws VSACException If there were any issues parsing.
   */
  public ValueSet parseValueSetResponse(InputStream response) throws VSACException {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      VSACSVSHandler svsHandler = new VSACSVSHandler();
      saxParser.parse(response, svsHandler);

      // The parser and requests have the ability to contain and parse multiple valuesets. But this will not happen in practice.
      List<ValueSet> valueSets = svsHandler.getParsedValueSets();

      // Grab the single value set.
      return valueSets.get(0);
    } catch(ParserConfigurationException pce) {
      throw new VSACException("Error setting up XML parser.", pce);
    } catch(SAXException saxe) {
      throw new VSACException("Error parsing value set response.", saxe);
    } catch(IOException ioe) {
      throw new VSACException("Error parsing value set response.", ioe);
    }
  }

  /**
   * Fetch a ValueSet and return it as a FHIR JSON string.
   * 
   * @param oid The ValueSet OID to fetch.
   * @return String with JSON content.
   * @throws VSACException If there was an error with any of the process.
   */
  public String getValueSetJSON(String oid) throws VSACException {
    ValueSet valueSet = this.getValueSet(oid);
    return ca.uhn.fhir.context.FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(valueSet);
  } 

  /**
   * Fetch a ValueSet from the VSAC API and return it as a FHIR R4 ValueSet.
   * 
   * @param oid The ValueSet OID to fetch.
   * @return FHIR R4 ValueSet resource.
   * @throws VSACException If there was an error with any of the process.
   */
  public ValueSet getValueSet(String oid) throws VSACException {
    // Build up request.
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
    
    // Handle response.
    ValueSet valueSet = null;
    try {
      CloseableHttpResponse response = this.client.execute(vsRequest);
      try {
        HttpEntity responseEntity = response.getEntity();
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 401) {
          // Service tickets only last 5 minutes. This is unlikely to happen.
          throw new VSACInvalidCredentialsException();
        } else if (statusCode == 200) {
          valueSet = this.parseValueSetResponse(responseEntity.getContent());
        } else if (statusCode == 404) {
          throw new VSACValueSetNotFoundException(oid);
        } else {
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

  /**
   * Getter for the loader's current Ticket Granting Ticket.
   * 
   * @return VSAC Ticket Granting Ticket
   */
  public String getTGT() {
    return this.ticketGrantingTicket;
  }

  /**
   * Closes the HttpClient if it decided to keep alive a connection. Should be done before discarding this loader.
   * 
   * @throws VSACException If there was an issue closing the connection.
   */
  public void close() throws VSACException {
    try {
      this.client.close();
    } catch(IOException ioe) {
      throw new VSACException("Error closing HTTP Client.", ioe);
    }
  }
}