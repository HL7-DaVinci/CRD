package org.hl7.davinci.endpoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.ZipException;
import org.hl7.ShortNameMaps;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.zeroturnaround.zip.ZipUtil;

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
  @Autowired
  public CommandLineRunner loadData(DataRepository repository, YamlConfig config) {
    return (args) -> {
      String pattern = "file:" + Paths.get(config.getLocalDbRules() ,"/*/*/*/").toAbsolutePath();
      ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(new FileSystemResourceLoader());
      Resource[] cqlFileResources = resolver.getResources(pattern);
      for (Resource cqlFileResource: cqlFileResources) {
        File zipF = File.createTempFile("crd_server_cql_package", ".zip");
        try {
          ZipUtil.pack(cqlFileResource.getFile(), zipF);
        } catch (Exception e) {
          continue;
        }
        String fileName = cqlFileResource.getFile().getName();
        String code = fileName.substring(0, fileName.indexOf('_') == -1 ? fileName.length() : fileName.indexOf('_'));
        String codeSystemShortName  = cqlFileResource.getFile().getParentFile().getName();
        String payorNameShortName = cqlFileResource.getFile().getParentFile().getParentFile().getName();
        String codeSystem = ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.get(codeSystemShortName);
        String payorName = ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.get(payorNameShortName);
        CoverageRequirementRule rule = new CoverageRequirementRule();
        rule.setPayor(payorName);
        rule.setCode(code);
        rule.setCodeSystem(codeSystem);
        rule.setCqlPackagePath(zipF.getAbsolutePath());
        repository.save(rule);
        System.out.println(String.format("Added rule %s, %s, %s",payorNameShortName,codeSystemShortName,code));
      }
    };
  }


}
