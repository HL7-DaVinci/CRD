package org.hl7.davinci.endpoint.controllers;

import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.components.FhirUriFetcher;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundleFile;
import org.hl7.davinci.endpoint.database.*;
import org.hl7.davinci.endpoint.files.FileResource;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleDownloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Provides the REST interface that can be interacted with at [base]/api/data.
 */
@RestController
public class DataController {
  private static Logger logger = Logger.getLogger(Application.class.getName());


  @Autowired
  private DataRepository repository;

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private YamlConfig myConfig;

  @Autowired
  private CoverageRequirementRuleDownloader downloader;

  @Autowired
  private FhirUriFetcher fhirUriFetcher;

  @Autowired
  private FileStore fileStore;

  /**
   * Basic constructor to initialize both data repositories.
   * @param repository the database for the data (rules)
   * @param requestRepository the database for request logging
   */
  @Autowired
  public DataController(DataRepository repository, RequestRepository requestRepository) {
    this.repository = repository;
    this.requestRepository = requestRepository;

  }

  @GetMapping(value = "/api/requests")
  @CrossOrigin
  public Iterable<RequestLog> showAllLogs() {
    // logger.info("showAllLogs: GET /api/requests");

    boolean[] timelineTrue = new boolean[5];
    Arrays.fill(timelineTrue, Boolean.TRUE);
    boolean[] timelineFalse = new boolean[5];
    Arrays.fill(timelineFalse, Boolean.FALSE);

    Iterable<RequestLog> list = requestRepository.findAll();
    return list;
  }

  @GetMapping(value = "/api/data")
  @CrossOrigin
  public Iterable<RuleMapping> showAll() {
    logger.info("showAll: GET /api/data");
    return fileStore.findAll();
  }

  /**
   * Gets some data from the repository.
   * @param id the id of the desired data.
   * @return the data from the repository
   */
  @CrossOrigin
  @GetMapping("/api/data/{id}")
  public CoverageRequirementRule getRule(@PathVariable long id) {
    logger.info("getRule: GET /api/data/" + id);
    Optional<CoverageRequirementRule> rule = repository.findById(id);

    if (!rule.isPresent()) {
      throw new RuleNotFoundException();
    }

    return rule.get();
  }

  public ResponseEntity<Resource> downloadFile(long id, String name) {
    CqlBundleFile bundleFile = downloader.downloadCqlBundleFile(id, name);

    if (bundleFile == null) {
      logger.warning("file not found, return error (404)");
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + bundleFile.getFilename() + "\"")
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(bundleFile.getResource());
  }

  @GetMapping(path = "/download/{id}")
  public ResponseEntity<Resource> download(@PathVariable long id) throws IOException {
    logger.info("download: GET /download/" + id);
    return downloadFile(id, "");
  }

  @GetMapping(path = "/getfileid/{id}/{name}")
  public ResponseEntity<Resource> getFileRuleId(@PathVariable long id, @PathVariable String name) throws IOException {
    logger.info("getfile: GET /getfileid/" + id + "/" + name);
    return downloadFile(id, name);
  }

  @GetMapping(path = "/getfile/{payer}/{codeSystem}/{code}/{name}")
  public ResponseEntity<Resource> getFile(@PathVariable String payer, @PathVariable String codeSystem, @PathVariable String code, @PathVariable String name) {
    logger.info("getfile: GET /getfile/" + payer + "/" + codeSystem + "/" + code + "/" + name);

    CqlBundleFile bundleFile = downloader.getFile(payer, codeSystem, code, name);

    if (bundleFile == null) {
      logger.warning("file not found, return error (404)");
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + bundleFile.getFilename() + "\"")
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(bundleFile.getResource());
  }

  @GetMapping(path = "/fetchFhirUri/{fhirUri}")
  public ResponseEntity<Resource> fetchFhirUri(@PathVariable String fhirUri) throws IOException {
    logger.info("download: GET /fetchFhirUri/" + fhirUri);

    Resource resource = fhirUriFetcher.fetch(fhirUri);

    if (resource == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fhirUri + "\"")
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(resource);
  }




  @GetMapping(path = "/fhir/{fhirVersion}/metadata")
  public ResponseEntity<Resource> getFhirResourceById(@PathVariable String fhirVersion) throws IOException {
    fhirVersion = fhirVersion.toUpperCase();
    logger.info("GET /fhir/" + fhirVersion + "/metadata");
    //TODO
    return ResponseEntity.ok().build();
  }

  @GetMapping(path = "/fhir/{fhirVersion}/{resource}/{id}")
  public ResponseEntity<Resource> getFhirResourceById(@PathVariable String fhirVersion, @PathVariable String resource, @PathVariable String id) throws IOException {
    fhirVersion = fhirVersion.toUpperCase();
    logger.info("GET /fhir/" + fhirVersion + "/" + resource + "/" + id);
    //TODO
    return ResponseEntity.ok().build();
  }

  @GetMapping(path = "/fhir/{fhirVersion}/{resource}") //?name={topic}
  public ResponseEntity<Resource> getFhirResourceByTopic(@PathVariable String fhirVersion, @PathVariable String resource, @RequestParam String name) throws IOException {
    fhirVersion = fhirVersion.toUpperCase();
    logger.info("GET /fhir/" + fhirVersion + "/" + resource + "?name=" + name);
    //TODO
    return ResponseEntity.ok().build();
  }

  @GetMapping(path = "/files/{topic}/{fhirVersion}/{fileName}")
  public ResponseEntity<Resource> getFile(@PathVariable String topic, @PathVariable String fhirVersion, @PathVariable String fileName, @RequestParam(required = false) boolean noconvert) throws IOException {
    logger.info("GET /files/" + topic + "/" + fhirVersion + "/" + fileName);

    FileResource fileResource = fileStore.getFile(topic, fileName, fhirVersion, !noconvert);

    if (fileResource == null) {
      logger.warning("file not found, return error (404)");
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileResource.getFilename() + "\"")
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(fileResource.getResource());
  }

  @GetMapping(path = "/reload")
  public ResponseEntity<Resource> reload() {
    logger.info("reload rule file index");
    fileStore.reload();
    return ResponseEntity.ok().build();
  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such rule") // 404
  public class RuleNotFoundException extends RuntimeException {
  }


}
