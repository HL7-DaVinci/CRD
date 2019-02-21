package org.hl7.davinci.endpoint;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.hl7.ShortNameMaps;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.DataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

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
   * Load all rules from the folder cql_rules, there should be a rule and rule info file.
   */
  @Bean
  public CommandLineRunner loadData(DataRepository repository) {
    return (args) -> {
      ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
      Resource[] cqlFileResources = resolver.getResources("/rules/cms/**/*.cql");
      for (Resource cqlFileResource: cqlFileResources){
        String fileName = cqlFileResource.getFile().getName();
        String code = fileName.substring(0,fileName.indexOf('_') == -1 ? fileName.indexOf('.') : fileName.indexOf('_'));
        String codeSystemShortName  = cqlFileResource.getFile().getParentFile().getName();
        String payorNameShortName = cqlFileResource.getFile().getParentFile().getParentFile().getName();
        String cql = new String(Files.readAllBytes(Paths.get(cqlFileResource.getURI())), StandardCharsets.UTF_8);

        String codeSystem = ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.get(codeSystemShortName);
        String payorName = ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.get(payorNameShortName);
        CoverageRequirementRule rule = new CoverageRequirementRule()
                                            .setPayor(payorName)
                                            .setCode(code)
                                            .setCodeSystem(codeSystem)
                                            .setCql(cql);
        repository.save(rule);
      }
    };
  }


}
