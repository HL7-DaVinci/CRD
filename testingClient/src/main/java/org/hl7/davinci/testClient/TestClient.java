package org.hl7.davinci.testClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.InputStream;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.hl7.davinci.r4.CrdRequestCreator;
import org.cdshooks.Card;
import org.cdshooks.CdsResponse;
import org.hl7.davinci.r4.crdhook.orderreview.OrderReviewRequest;
import org.hl7.fhir.r4.model.Enumerations;

public class TestClient {
  /**
   * Sets up the context and client and runs the test.
   * @param args main function args
   */
  public static void main(String[] args) throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.set(1948, Calendar.JULY, 4);
    OrderReviewRequest hookRequest = CrdRequestCreator
            .createOrderReviewRequest(Enumerations.AdministrativeGender.MALE, cal.getTime());

    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost("http://localhost:8090/cds-services/order-review-crd");

    ObjectMapper mapper = new ObjectMapper();
    ObjectWriter w = mapper.writer();
    
    HttpEntity entity = new StringEntity(w.writeValueAsString(hookRequest));
    httpPost.setEntity(entity);
    httpPost.setHeader("Accept", "application/json");
    httpPost.setHeader("Content-type", "application/json");

    CloseableHttpResponse response = client.execute(httpPost);
    InputStream responseStream = response.getEntity().getContent();

    CdsResponse cdsResponse = mapper.readValue(responseStream, CdsResponse.class);
    for (Card card : cdsResponse.getCards()) {
      System.out.println("Card Summary: " + card.getSummary());
      System.out.println("Card Detail: " + card.getDetail());
    }

  }

}