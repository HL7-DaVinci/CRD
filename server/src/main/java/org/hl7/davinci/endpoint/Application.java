package org.hl7.davinci.endpoint;

import org.hl7.davinci.endpoint.files.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
// Finds the FhirServlet and runs it
@ServletComponentScan
/**
 * Runs the app, including the FhirServlet.  Keep this in the root directory
 * or it won't find the dependencies.  Only initialization functions should be put here.
 */
public class Application {

  @Autowired
  FileStore fileStore;

  static final Logger logger =
      LoggerFactory.getLogger(Application.class);


  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }


  /**
   * Load the rules into the database.
   * @return
   */
  @Bean
  @Autowired
  public CommandLineRunner setup() {
    return (args) -> {
      fileStore.reload();
    };
  }

}
