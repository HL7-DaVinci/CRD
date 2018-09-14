package org.hl7.davinci.creator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

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
  public static int main(String[] args) throws Exception {
    String outputPath = args[0];
    String version = args[1];

    boolean makeR4 = version.equalsIgnoreCase("r4");
    boolean makeStu3 = version.equalsIgnoreCase("stu3");
    if (!makeR4 && !makeStu3) {
      System.out.println("Second argument should be r4 or stu3");
      return 1;
    }

    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    if (outputPath == null) {
      outputPath = Paths.get(".").toAbsolutePath().normalize().toString();
    }
    ObjectMapper mapper = new ObjectMapper();
    ObjectWriter w = mapper.writer();
    String filename = "crd_request_" + version +".json";
    FileWriter jsonWriter = new FileWriter(outputPath + "/" + filename);

    if (makeR4) {
      org.hl7.davinci.r4.crdhook.orderreview.OrderReviewRequest request =
          org.hl7.davinci.r4.CrdRequestCreator.createOrderReviewRequest(
              org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.MALE, cal.getTime());
      w.writeValue(jsonWriter, request);
    }
    if (makeStu3) {
      org.hl7.davinci.stu3.crdhook.orderreview.OrderReviewRequest request =
          org.hl7.davinci.stu3.CrdRequestCreator.createOrderReviewRequest(
              org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender.MALE, cal.getTime());
      w.writeValue(jsonWriter, request);
    }

    System.out.println("Wrote file '"+filename+"' to path '"+outputPath+"'");
    jsonWriter.close();
    return 1;
  }
}
