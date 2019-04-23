package org.hl7.davinci.endpoint.controllers;

import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.components.FhirUriFetcher;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundleFile;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.DataRepository;
import org.hl7.davinci.endpoint.database.RequestLog;
import org.hl7.davinci.endpoint.database.RequestRepository;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleDownloader;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
  private CoverageRequirementRuleFinder ruleFinder;

  @Autowired
  private CoverageRequirementRuleDownloader downloader;

  @Autowired
  private FhirUriFetcher fhirUriFetcher;


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
    logger.info("showAllLogs: GET /api/requests");
    return requestRepository.findAll();
  }

  @GetMapping(value = "/api/data")
  @CrossOrigin
  public Iterable<CoverageRequirementRule> showAll() {
    logger.info("showAll: GET /api/data");
    return ruleFinder.findAll();
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

  @GetMapping(path = "/getfile/{id}/{name}")
  public ResponseEntity<Resource> getFile(@PathVariable long id, @PathVariable String name) throws IOException {
    logger.info("getfile: GET /getfile/" + id + "/" + name);
    return downloadFile(id, name);
  }

  @GetMapping(path = "/fetchFhirUri/{fhirUri}")
  public ResponseEntity<Resource> fetchFhirUri(@PathVariable String fhirUri) throws IOException {
    logger.info("download: GET /fetchFhirUri/" + fhirUri);

    Resource resource = fhirUriFetcher.fetch(fhirUri);

    if (resource == null){
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fhirUri + "\"")
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(resource);
  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such rule")  // 404
  public class RuleNotFoundException extends RuntimeException {
  }


}
