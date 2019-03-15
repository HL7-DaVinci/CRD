package org.hl7.davinci.endpoint.cql.bundle;

import org.springframework.core.io.Resource;

public class CqlBundleFile {
  private Resource resource;
  private String filename;

  public Resource getResource() {
    return resource;
  }

  public CqlBundleFile setResource(Resource resource) {
    this.resource = resource;
    return this;
  }

  public String getFilename() {
    return filename;
  }

  public CqlBundleFile setFilename(String filename) {
    this.filename = filename;
    return this;
  }
}
