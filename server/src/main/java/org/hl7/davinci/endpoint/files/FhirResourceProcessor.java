package org.hl7.davinci.endpoint.files;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * Abstract interface for making changes to a FHIR resource. This takes care of the parsing
 * of the resource and encoding back out to a deliverable buffer. The implementing classes
 * just need to implement processResource and work with the FHIR models solely.
 * 
 * @param <T> Any FHIR R4 Resource model.
 */
public abstract class FhirResourceProcessor<T extends Resource> {

  static final Logger logger = LoggerFactory.getLogger(FhirResourceProcessor.class);

  /**
   * Implemented by the concrete processor. Makes any changes to the FHIR Resource and returns the
   * modified Resource
   * 
   * @param inputResource The Resource to modify.
   * @param fileStore The FileStore that may be used if other resources are needed.
   * @param baseUrl The base url of the server, usually obtained by the current request.
   * @return The new or modified FHIR Resource.
   */
  protected abstract T processResource(T inputResource, FileStore fileStore, String baseUrl);

  private FhirContext ctx;
  private IParser parser;

  public FhirResourceProcessor() {
    this.ctx = new org.hl7.davinci.r4.FhirComponents().getFhirContext();
    this.parser = ctx.newJsonParser().setPrettyPrint(true);
  }

  /**
   * Called by CommonFileStore/other users. This is the main entry point for the use of the processor.
   * Will call the abstract `processResource` to actually do the work.
   * 
   * @param inputFileResource The FileResource that will be parsed, then modified.
   * @param fileStore The file store to be used if any other resources need to be pulled for modifications.
   * @param baseUrl The base url of the server, usually obtained by the current request.
   * @return The new FileResource after modification.
   */
  public FileResource processResource(FileResource inputFileResource, FileStore fileStore, String baseUrl) {
    T inputResource = (T) this.parseFhirFileResource(inputFileResource);
    T outputResource = this.processResource(inputResource, fileStore, baseUrl);

    byte[] resourceData = parser.encodeResourceToString(outputResource).getBytes(Charset.defaultCharset());
    FileResource outputFileResource = new FileResource();
    outputFileResource.setResource(new ByteArrayResource(resourceData));
    outputFileResource.setFilename(inputFileResource.getFilename());
    return outputFileResource;
  }

  /**
   * Parses a FHIR resource from a FileResource.
   * 
   * @param fileResource The FileResource to parse.
   * @return The parsed FHIR R4 model.
   */
  protected Resource parseFhirFileResource(FileResource fileResource) {
    try {
      return (Resource) parser.parseResource(fileResource.getResource().getInputStream());
    } catch(IOException ioe) {
      logger.error("Issue parsing FHIR file resource for preprocessing.", ioe);
      return null;
    }
  }
}