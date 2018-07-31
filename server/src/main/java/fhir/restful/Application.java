package fhir.restful;

import fhir.restful.database.DataRepository;
import fhir.restful.database.Datum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;



@SpringBootApplication
// Finds the FhirServlet and runs it
@ServletComponentScan

/**
 * Runs the app, including the FhirServlet.  Keep this in the root directory
 * or it won't find the dependencies.  Only initialization functions should be put here.
 */
public class Application {


  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  /**
   * Loads data into the repository.
   * @param repository the repository to put the data in.
   * @return arguments to put into the command line runner of Spring
   */
  @Bean
  public CommandLineRunner loadData(DataRepository repository) {
    return (args) -> {
      // save a couple of customers
      readCsv(repository);
      repository.save(new Datum("18", "80", "MF", "219", "94660", "false",
           "https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf"));
      repository.save(new Datum("18", "90", "MF", "002", "97542", "true",
          "https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PMDFactSheet07_Quark19.pdf"
      ));
    };
  }


  /**
   * Reads the csv file with the data to put in the repository and populates the repository.
   * @param repository the repository to populate with data from the csv
   */
  public void readCsv(DataRepository repository) {

    String csvFile = "crd_table_basic.csv";
    try {
      File file = new ClassPathResource("crd_table_basic.csv").getFile();
      csvFile = file.getCanonicalPath();
    } catch (IOException e) {
      e.printStackTrace();
    }
    String line = "";
    String cvsSplitBy = ",";


    try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

      br.readLine(); // Remove header
      while ((line = br.readLine()) != null) {

        // use comma as separator
        String[] c = line.split(cvsSplitBy);

        repository.save(new Datum(c[0], c[1], c[2], c[3], c[4], c[5], c[6]));

      }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }


}
