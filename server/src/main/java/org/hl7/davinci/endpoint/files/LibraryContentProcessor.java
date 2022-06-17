package org.hl7.davinci.endpoint.files;

import org.apache.commons.codec.binary.Base64;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.Library;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Processes FHIR R4 Library to have CQL files embedded.
 */
public class LibraryContentProcessor extends FhirResourceProcessor<Library> {

  static final Logger logger = LoggerFactory.getLogger(LibraryContentProcessor.class);

  /**
   * Processes the Library to have content pointing to CQL replaced with embedded base64 encoded CQL file.
   * Only supports relative paths to files being hosted on this server.
   */
  @Override
  protected Library processResource(Library inputResource, FileStore fileStore, String baseUrl) {

    Library output = inputResource.copy();
    List<Attachment> content = inputResource.getContent();

    logger.info("Attempt to embed CQL (ELM) into Requested Library");

    // if the first value in content is application/elm+json with a url, replace it with base64 encoded data
    if (content.size() > 0) {
      Attachment attachment = content.get(0);
      if (attachment.hasUrl()) {
        String url = attachment.getUrl();

        // make sure this is a relative path
        if (!url.toUpperCase().startsWith("HTTP")) {

          // grab the topic, fhir version, and filename from the url
          String[] urlParts = url.split("/");
          if (urlParts.length >= 4) {

            if ( (attachment.getContentType().equalsIgnoreCase("application/elm+json"))
                || (attachment.getContentType().equalsIgnoreCase("text/cql")) ){

              // content is CQL (assuming elm/json is actually CQL)
              String topic = urlParts[1];
              String fhirVersion = urlParts[2].toUpperCase();
              String fileName = urlParts[3];

              List<Attachment> attachments = new ArrayList<>();

              // get the CQL data and base64 encode
              FileResource cqlFileResource = fileStore.getFile(topic, fileName, fhirVersion, false);
              attachments.add(base64EncodeToAttachment(cqlFileResource, "text/cql"));

              // get the ELM data and base64 encode
              FileResource elmFileResource = fileStore.getFile(topic, fileName, fhirVersion, true);
              attachments.add(base64EncodeToAttachment(elmFileResource, "application/elm+json"));

              // insert back into the Library
              output.setContent(attachments);

            } else {
              logger.info("Content is not xml or json elm");
            }
          } else {
            logger.info("URL doesn't split properly: " + url);
          }
        } else {
          logger.info("URL is NOT relative");
        }
      } else {
        logger.info("Content is not a url");
      }
    } else {
      logger.info("No content in library");
    }

    return output;
  }

  private Attachment base64EncodeToAttachment(FileResource fileResource, String mimeType) {
    Attachment attachment = new Attachment();
    try {
      // base64 encode
      InputStream inputStream = fileResource.getResource().getInputStream();  
      byte[] byteData = new byte[inputStream.available()];
      inputStream.read(byteData);
      String encodedData = Base64.encodeBase64String(byteData);
      attachment.setContentType(mimeType);
      Base64BinaryType b64bType = new Base64BinaryType();
      b64bType.setValueAsString(encodedData);
      attachment.setDataElement(b64bType);
    } catch (IOException e) {
      logger.warn("failed to read the data: " + mimeType);
    }
    return attachment;
  }
}


