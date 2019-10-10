package org.hl7.davinci.endpoint.github;

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
@Profile("gitHub")
public class GitHubFhirUriFetcher implements FhirUriFetcher {

  private static Logger logger = Logger.getLogger(Application.class.getName());

  YamlConfig config;

  FhirUriFetcherLocal localFetcher;

  @Autowired
  public GitHubFhirUriFetcher(YamlConfig yamlConfig) {
    config = yamlConfig;
    localFetcher = new FhirUriFetcherLocal(yamlConfig);
  }

  public Resource fetch(String fhirUri) {
    logger.info("GitHub FHIR URI Fetching not yet implemented, using local fetcher");
    //TODO: copy the implementation from FhirUriFetcherLocal and call GitHubConnection::getFile()
    return localFetcher.fetch(fhirUri);
  }
}