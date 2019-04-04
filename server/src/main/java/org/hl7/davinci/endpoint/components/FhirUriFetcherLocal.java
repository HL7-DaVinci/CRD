package org.hl7.davinci.endpoint.components;

import java.io.File;
import java.nio.file.Paths;
import java.util.logging.Logger;
import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.YamlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Profile("localDb")
public class FhirUriFetcherLocal implements FhirUriFetcher {
  private static Logger logger = Logger.getLogger(Application.class.getName());


  YamlConfig config;

  @Autowired
  public FhirUriFetcherLocal(YamlConfig yamlConfig) {
    config = yamlConfig;
  }

  public Resource fetch(String fhirUri){
    if (!fhirUri.startsWith("urn:hl7:davinci:crd:")){
      //TODO: eventually this should support other fhir uri/url, it could just fetch them in json format and return them
      return null;
    }
    fhirUri = fhirUri.replace("urn:hl7:davinci:crd:","");
    if (!fhirUri.endsWith(".json")) {
      fhirUri = fhirUri + ".json";
    }
    File file = Paths.get(config.getLocalDbFhirArtifacts(),fhirUri).toFile();
    if (!file.exists()){
      return null;
    }
    return new FileSystemResource(file);
  }


}
