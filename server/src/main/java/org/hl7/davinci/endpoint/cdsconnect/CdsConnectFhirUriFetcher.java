package org.hl7.davinci.endpoint.cdsconnect;


import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.components.FhirUriFetcher;
import org.hl7.davinci.endpoint.components.FhirUriFetcherLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
@Profile("cdsConnect")
public class CdsConnectFhirUriFetcher implements FhirUriFetcher {

  private static Logger logger = Logger.getLogger(Application.class.getName());


  YamlConfig config;

  //@Autowired
  FhirUriFetcherLocal localFetcher;

  @Autowired
  public CdsConnectFhirUriFetcher(YamlConfig yamlConfig) {
    config = yamlConfig;
    localFetcher = new FhirUriFetcherLocal(yamlConfig);
  }

  public Resource fetch(String fhirUri){
    logger.info("CDS Connect FHIR URI Fetching not yet implemented, using local fetcher");
    return localFetcher.fetch(fhirUri);
  }
}
