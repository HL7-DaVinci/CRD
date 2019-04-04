package org.hl7.davinci.endpoint.components;

import org.springframework.core.io.Resource;

public interface FhirUriFetcher {

  Resource fetch(String fhirUri);

}
