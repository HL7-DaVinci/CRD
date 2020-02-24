package org.hl7.davinci.endpoint.files;

import org.springframework.core.io.Resource;

public class FileResource {
  private Resource resource;
  private String filename;

  public Resource getResource() {
    return resource;
  }

  public FileResource setResource(Resource resource) {
    this.resource = resource;
    return this;
  }

  public String getFilename() {
    return filename;
  }

  public FileResource setFilename(String filename) {
    this.filename = filename;
    return this;
  }
}
