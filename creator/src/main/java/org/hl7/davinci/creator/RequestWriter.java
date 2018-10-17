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
  public static void main(String[] args) throws Exception {
    String outputPath = args[0];
    String version = args[1];

    boolean makeR4 = version.equalsIgnoreCase("r4");
    boolean makeStu3 = version.equalsIgnoreCase("stu3");
    if (!makeR4 && !makeStu3) {
      System.out.println("Second argument should be r4 or stu3");
      return;
    }

    Calendar cal = Calendar.getInstance();
    cal.set(1970, Calendar.JULY, 4);
    if (outputPath == null) {
      outputPath = Paths.get(".").toAbsolutePath().normalize().toString();
    }
    ObjectMapper mapper = new ObjectMapper();
    ObjectWriter w = mapper.writer();

    if (makeR4) {
      String filename = "crd_order_review_request_" + version + ".json";
      org.hl7.davinci.r4.crdhook.orderreview.OrderReviewRequest ord_request =
          org.hl7.davinci.r4.CrdRequestCreator.createOrderReviewRequest(
              org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.MALE, cal.getTime(), "MA", "MA");
      w.writeValue(new FileWriter(outputPath + "/" + filename), ord_request);
      System.out.println("Wrote file '" + filename + "' to path '" + outputPath + "'");

      filename = "crd_medication_request_" + version + ".json";
      org.hl7.davinci.r4.crdhook.medicationprescribe.MedicationPrescribeRequest med_request =
          org.hl7.davinci.r4.CrdRequestCreator.createMedicationPrescribeRequest(
              org.hl7.fhir.r4.model.Enumerations.AdministrativeGender.MALE, cal.getTime(), "MA", "MA");
      w.writeValue(new FileWriter(outputPath + "/" + filename), med_request);
      System.out.println("Wrote file '" + filename + "' to path '" + outputPath + "'");
    }
    if (makeStu3) {
      String filename = "crd_order_review_request_" + version + ".json";
      org.hl7.davinci.stu3.crdhook.orderreview.OrderReviewRequest ord_request =
          org.hl7.davinci.stu3.CrdRequestCreator.createOrderReviewRequest(
              org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender.MALE, cal.getTime(), "MA", "MA");
      w.writeValue(new FileWriter(outputPath + "/" + filename), ord_request);
      System.out.println("Wrote file '" + filename + "' to path '" + outputPath + "'");

      filename = "crd_medication_request_" + version + ".json";
      org.hl7.davinci.stu3.crdhook.medicationprescribe.MedicationPrescribeRequest med_request =
          org.hl7.davinci.stu3.CrdRequestCreator.createMedicationPrescribeRequest(
              org.hl7.fhir.dstu3.model.Enumerations.AdministrativeGender.MALE, cal.getTime(), "MA", "MA");
      w.writeValue(new FileWriter(outputPath + "/" + filename), med_request);
      System.out.println("Wrote file '" + filename + "' to path '" + outputPath + "'");
    }
  }
}
