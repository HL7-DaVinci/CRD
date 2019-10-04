package org.hl7.davinci.endpoint;

import java.io.File;
import java.io.IOException;

import org.hl7.ShortNameMaps;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.DataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.zeroturnaround.zip.ZipUtil;

@SpringBootApplication
// Finds the FhirServlet and runs it
@ServletComponentScan
/**
 * Runs the app, including the FhirServlet.  Keep this in the root directory
 * or it won't find the dependencies.  Only initialization functions should be put here.
 */
public class Application {

  static final Logger logger =
      LoggerFactory.getLogger(Application.class);


  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }



  /**
   * Load all rules from the folder cql_rules, there should be a rule and rule info file.
   */
  @Bean
  @Autowired
  @Profile("localDb")
  public CommandLineRunner loadData(DataRepository repository, YamlConfig config) {
    return (args) -> {

      File[] payers = new File(config.getLocalDbRules()).listFiles();
      for (File payer: payers) {
        if (payer.isDirectory()) {

          File[] codeSystems = payer.listFiles();
          for (File codeSystem: codeSystems) {
            if (codeSystem.isDirectory()) {

              File[] codes = codeSystem.listFiles();
              for (File code: codes) {
                if (code.isDirectory()) {
                  loadResource(repository, code);
                }
              }
            }
          }
        }
      }
    };
  }

  /**
   * Add file into the localDb
   * @param repository that contains the rules
   * @param file to process as a rule
   */
  private void loadResource(DataRepository repository, File file) {
    try {
      File zipF = File.createTempFile("crd_server_cql_package", ".zip");
      try {
        ZipUtil.pack(file, zipF);
      } catch (Exception e) {
        return;
      }
      String fileName = file.getName();
      String code = fileName.substring(0, fileName.indexOf('_') == -1 ? fileName.length() : fileName.indexOf('_'));
      String codeSystemShortName = file.getParentFile().getName();
      String payorNameShortName = file.getParentFile().getParentFile().getName();
      String codeSystem = ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.get(codeSystemShortName);
      String payorName = ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.get(payorNameShortName);
      CoverageRequirementRule rule = new CoverageRequirementRule();
      rule.setPayor(payorName);
      rule.setCode(code);
      rule.setCodeSystem(codeSystem);
      rule.setCqlPackagePath(zipF.getAbsolutePath());
      repository.save(rule);
      logger.info(String.format("Added rule %s, %s, %s", payorNameShortName, codeSystemShortName, code));
    } catch (IOException e) {
      logger.info("failed to add file: " + file.toString() + "\n" + e.toString());
    }
  }

}
