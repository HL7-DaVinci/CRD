package org.hl7.davinci.creator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.hl7.davinci.r4.CrdRequestCreator;
import org.hl7.davinci.r4.crdhook.orderreview.OrderReviewRequest;
import org.hl7.fhir.r4.model.Enumerations;


import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Calendar;

/**
 * Small application that will write out a sample CRD request in JSON and XML.
 *
 * The first argument to the application is the desired directory to write the files into. If one is not provided,
 * the application will use the current working directory.
 */
public class RequestWriter {
  /**
   * Main method for the application.
   * @param args command line arguments
   * @throws Exception If there is an issue writing the file
   */
  public static void main(String[] args) throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    OrderReviewRequest request = CrdRequestCreator.createOrderReviewRequest(Enumerations.AdministrativeGender.MALE, cal.getTime());
    ObjectMapper mapper = new ObjectMapper();
    ObjectWriter w = mapper.writer();
    String outputPath = args[0];
    if (outputPath == null) {
      outputPath = Paths.get(".").toAbsolutePath().normalize().toString();
    }
    FileWriter jsonWriter = new FileWriter(outputPath + "/crd_request.json");
    w.writeValue(jsonWriter, request);
    jsonWriter.close();
  }
}
