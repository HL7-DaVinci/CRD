package org.hl7.davinci.endpoint.controllers;

import org.hl7.davinci.FhirResourceInfo;
import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.Utils;
import org.hl7.davinci.endpoint.database.FhirResource;
import org.hl7.davinci.endpoint.database.FhirResourceRepository;
import org.hl7.davinci.endpoint.fhir.r4.QuestionnaireNextQuestionOperation;
import org.hl7.davinci.endpoint.fhir.r4.QuestionnairePackageOperation;
import org.hl7.davinci.endpoint.files.FileResource;
import org.hl7.davinci.endpoint.files.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;


/**
 * Provides the REST interface that can be interacted with at [base]/api/fhir and [base]/fhir.
 */
@RestController
public class FhirController {
  private static Logger logger = Logger.getLogger(Application.class.getName());

  @Autowired
  private FileStore fileStore;

  @Autowired
  private FhirResourceRepository fhirResourceRepository;

  org.hl7.davinci.endpoint.fhir.r4.Metadata r4Metadata = new org.hl7.davinci.endpoint.fhir.r4.Metadata();


  @GetMapping(value = "/api/fhir")
  @CrossOrigin
  public Iterable<FhirResource> showAllFhir() {
    logger.info("showAllFhir: GET /api/fhir");
    return fileStore.findAllFhirResources();
  }

  @GetMapping(path = "/fhir/{fhirVersion}/metadata")
  public ResponseEntity<String> getConformanceStatement(HttpServletRequest request, @PathVariable String fhirVersion) throws IOException {
    fhirVersion = fhirVersion.toUpperCase();
    logger.info("GET /fhir/" + fhirVersion + "/metadata");
    String baseUrl = Utils.getApplicationBaseUrl(request).toString() + "/";

    String json = "";
    if (fhirVersion.equalsIgnoreCase("R4")) {
      json = r4Metadata.getMetadata(baseUrl);
    } else {
      logger.warning("Unsupported FHIR version: " + fhirVersion);
    }

    return new ResponseEntity<String>(json, HttpStatus.OK);
  }
  /**
   * Get a FHIR ValueSet expansion by canonical URL. This pretends to be a ValueSet/$expand operator.
   *
   * @param url The Canonical URL of the ValueSet.
   * @return
   */
  @GetMapping(path = "fhir/r4/ValueSet/$expand")
  public ResponseEntity<Resource> getFhirValueSetExpansion(HttpServletRequest request, @RequestParam String url){
    String baseUrl = Utils.getApplicationBaseUrl(request).toString() + "/";
    logger.info("GET /fhir/R4/ValueSet/$expand");

    if (url != null) {
      // If URL starts with this server's base url, pull out id and search by id
      if (url.startsWith(baseUrl)) {
        String valueSetId = url.split("ValueSet/")[1];
        FileResource fileResource = fileStore.getFhirResourceById("R4", "valueset", valueSetId, baseUrl);
        return processFileResource(fileResource);

        // If the URL is from elsewhere, look by URL
      } else {
        FileResource fileResource = fileStore.getFhirResourceByUrl("R4", "valueset", url, baseUrl);
        return processFileResource(fileResource);
      }

      // if the URL was not provided, we cannot provide an expansion. return 401 bad request
    } else {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Retrieve a FHIR resource by id
   * @param fhirVersion (converted to uppercase)
   * @param resource (converted to lowercase)
   * @param id (converted to lowercase)
   * @return
   * @throws IOException
   */
  @GetMapping(path = "/fhir/{fhirVersion}/{resource}/{id}")
  public ResponseEntity<Resource> getFhirResourceById(HttpServletRequest request, @PathVariable String fhirVersion, @PathVariable String resource, @PathVariable String id) throws IOException {
    fhirVersion = fhirVersion.toUpperCase();
    resource = resource.toLowerCase();
    logger.info("GET /fhir/" + fhirVersion + "/" + resource + "/" + id);
    String baseUrl = Utils.getApplicationBaseUrl(request).toString() + "/";

    FileResource fileResource = fileStore.getFhirResourceById(fhirVersion, resource, id, baseUrl);
    return processFileResource(fileResource);
  }

  /**
   * Retrieve a FHIR resource by name.
   * @param fhirVersion (converted to uppercase)
   * @param resource (converted to lowercase)
   * @param name (converted to lowercase)
   * @param url The Canonical URL of the resource.
   * @return
   * @throws IOException
   */
  @GetMapping(path = "/fhir/{fhirVersion}/{resource}") //?name={TopicFormName}
  public ResponseEntity<Resource> searchFhirResource(HttpServletRequest request, @PathVariable String fhirVersion,
                                                     @PathVariable String resource, @RequestParam(required = false) String name, @RequestParam(required = false) String url, @RequestParam(required = false) String topic) throws IOException {

    fhirVersion = fhirVersion.toUpperCase();
    resource = resource.toLowerCase();
    String baseUrl = Utils.getApplicationBaseUrl(request).toString() + "/";

    FileResource fileResource = null;

    if (name != null) {
      name = name.toLowerCase();
      logger.info("GET /fhir/" + fhirVersion + "/" + resource + "?name=" + name);
      fileResource = fileStore.getFhirResourceByName(fhirVersion, resource, name, baseUrl);
    }
    else if (url != null) {
      logger.info("GET /fhir/" + fhirVersion + "/" + resource + "?url=" + url);
      fileResource = fileStore.getFhirResourceByUrl(fhirVersion, resource, url, baseUrl);
    }
    else if (topic != null) {
      topic = topic.toLowerCase();
      logger.info("GET /fhir/" + fhirVersion + "/" + resource + "?topic=" + topic);
      fileResource = fileStore.getFhirResourcesByTopicAsBundle(fhirVersion, resource, topic, baseUrl);
    }

    return processFileResource(fileResource);
  }

  private ResponseEntity<Resource> processFileResource(FileResource fileResource) {
    if (fileResource == null) {
      logger.warning("file / fhir resource not found, return error (404)");
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResource.getFilename() + "\"")
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(fileResource.getResource());
  }

  /**
   * Store a FHIR resource.
   */
  @PostMapping(path = "/fhir/{fhirVersion}/{resource}", consumes = { MediaType.APPLICATION_JSON_VALUE, "application/fhir+json" })
  public ResponseEntity<String> submitFhirResource(HttpServletRequest request, HttpEntity<String> entity,
                                                   @PathVariable String fhirVersion, @PathVariable String resource) {

    fhirVersion = fhirVersion.toUpperCase();
    resource = resource.toLowerCase();

    logger.info("POST /fhir/" + fhirVersion + "/" + resource);

    FhirResourceInfo fhirResourceInfo = new FhirResourceInfo();

    // pull out the ID and name
    if (fhirVersion.equalsIgnoreCase("R4")) {
      fhirResourceInfo = org.hl7.davinci.r4.Utilities.getFhirResourceInfo(entity.getBody());
    } else {
      logger.warning("unsupported FHIR version: " + fhirVersion + ", not storing");
      HttpStatus status = HttpStatus.BAD_REQUEST;
      MediaType contentType = MediaType.TEXT_PLAIN;

      return ResponseEntity.status(status).contentType(contentType)
          .body("Bad Request");
    }

    if (!fhirResourceInfo.valid()) {
      logger.warning("submitFhirResource: unsupported FHIR Resource of type: " + fhirResourceInfo.getType() + " (" + fhirVersion + "), not storing");
      HttpStatus status = HttpStatus.BAD_REQUEST;
      MediaType contentType = MediaType.TEXT_PLAIN;

      return ResponseEntity.status(status).contentType(contentType)
          .body("Bad Request");
    }

    // build the FhirResource and save to the database
    FhirResource fhirResource = new FhirResource();
    fhirResource.setFhirVersion(fhirVersion)
        .setResourceType(fhirResourceInfo.getType())
        .setData((entity.getBody()));
    if (fhirResourceInfo.getId() != null) {
      fhirResource.setId(fhirResourceInfo.getId());
    }
    if (fhirResourceInfo.getName() != null) {
      fhirResource.setName(fhirResourceInfo.getName());
    }
    FhirResource newFhirResource = fhirResourceRepository.save(fhirResource);

    String newId = newFhirResource.getId();
    String statusMsg = "successfully stored " + fhirResourceInfo.getType() + ": " + newId;
    logger.info(statusMsg);

    // build the response
    HttpStatus status = HttpStatus.CREATED;
    MediaType contentType = MediaType.TEXT_PLAIN;
    String baseUrl = Utils.getApplicationBaseUrl(request).toString() + "/";

    return ResponseEntity.status(status).contentType(contentType)
        .header(HttpHeaders.LOCATION, baseUrl + "/fhir/" + fhirVersion + "/" + resource + "?identifier=" + newId)
        .body(statusMsg);
  }

  /**
   * FHIR Operation to retrieve all of the Questionnaires and CQL files associated with a given request.
   * @param fhirVersion (converted to uppercase)
   * @return FHIR Bundle
   * @throws IOException
   */
  @PostMapping(path = "/fhir/{fhirVersion}/Questionnaire/$questionnaire-package", consumes = { MediaType.APPLICATION_JSON_VALUE, "application/fhir+json" })
  public ResponseEntity<String> questionnaireForOrderOperation(HttpServletRequest request, HttpEntity<String> entity,
                                                   @PathVariable String fhirVersion) {

    fhirVersion = fhirVersion.toUpperCase();
    String baseUrl = Utils.getApplicationBaseUrl(request).toString() + "/";

    logger.info("POST /fhir/" + fhirVersion + "/Questionnaire/$Questionnaire-package");

    String resource = null;
    if (fhirVersion.equalsIgnoreCase("R4")) {
      QuestionnairePackageOperation operation = new QuestionnairePackageOperation(fileStore, baseUrl);
      resource = operation.execute(entity.getBody());

      if (resource == null) {
        logger.warning("bad parameters");
        HttpStatus status = HttpStatus.BAD_REQUEST;
        MediaType contentType = MediaType.TEXT_PLAIN;
        
        return ResponseEntity.status(status).contentType(contentType)
            .body("Bad Parameters");
      }

    } else {
      logger.warning("unsupported FHIR version: " + fhirVersion + ", not storing");
      HttpStatus status = HttpStatus.BAD_REQUEST;
      MediaType contentType = MediaType.TEXT_PLAIN;

      return ResponseEntity.status(status).contentType(contentType)
          .body("Bad Request");
    }

    return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
          .body(resource);
  }


  @PostMapping(value = "/fhir/{fhirVersion}/Questionnaire/$next-question", consumes = { MediaType.APPLICATION_JSON_VALUE, "application/fhir+json" })
  public ResponseEntity<String> retrieveNextQuestion(HttpServletRequest request, HttpEntity<String> entity, @PathVariable String fhirVersion) {
    fhirVersion = fhirVersion.toUpperCase();
    
    logger.info("POST /fhir/" + fhirVersion + "/Questionnaire/$next-question");

    if (fhirVersion.equalsIgnoreCase("R4")) {
      QuestionnaireNextQuestionOperation operation = new QuestionnaireNextQuestionOperation(fileStore);
      return operation.execute(entity.getBody(), request);
    } else {
      logger.warning("unsupported FHIR version: " + fhirVersion + ", not storing");
      HttpStatus status = HttpStatus.BAD_REQUEST;
      MediaType contentType = MediaType.TEXT_PLAIN;

      return ResponseEntity.status(status).contentType(contentType)
          .body("Bad Request");
    }
  }
}
