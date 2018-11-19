package org.hl7.davinci.endpoint.controllers;

import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.DataRepository;

import org.hl7.davinci.endpoint.database.RequestLog;
import org.hl7.davinci.endpoint.database.RequestRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
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
    return requestRepository.findAll();
  }

  @GetMapping(value = "/api/data")
  @CrossOrigin
  public Iterable<CoverageRequirementRule> showAll() {
    return repository.findAll();
  }

  /**
   * Gets some data from the repository.
   * @param id the id of the desired data.
   * @return the data from the repository
   */
  @CrossOrigin
  @GetMapping("/api/data/{id}")
  public CoverageRequirementRule getRule(@PathVariable long id) {
    Optional<CoverageRequirementRule> rule = repository.findById(id);

    if (!rule.isPresent()) {
      throw new RuleNotFoundException();
    }

    return rule.get();
  }

  /**
   * Allows post requests to add data to the repository.
   * @param rule the object to put into the repository
   * @return the response from the server
   */
  @PostMapping("/api/data")
  public ResponseEntity<Object> addRule(@RequestBody CoverageRequirementRule rule) {
    CoverageRequirementRule savedDatum = repository.save(rule);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
        .buildAndExpand(savedDatum.getId()).toUri();
    return ResponseEntity.created(location).build();
  }

  @DeleteMapping("/api/data/{id}")
  public long deleteRule(@PathVariable long id) {
    repository.deleteById(id);
    return id;
  }

  /**
   * Allows updated of data through the REST API.
   * @param rule the new data
   * @param id the id of the data to be replaced
   * @return the response from the server
   */
  @PutMapping("/api/data/{id}")
  public ResponseEntity<Object> updateRule(@RequestBody CoverageRequirementRule rule,
      @PathVariable long id) {
    Optional<CoverageRequirementRule> datumOptional = repository.findById(id);

    if (!datumOptional.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    rule.setId(id);
    repository.save(rule);
    return ResponseEntity.noContent().build();
  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such rule")  // 404
  public class RuleNotFoundException extends RuntimeException {
  }

  @GetMapping("/user/me")
  public Principal user(Principal principal) {

    return principal;
  }



  @CrossOrigin
  @RequestMapping(value = "/api/testing", method = RequestMethod.POST)
  public String testingApi(@RequestBody Object object) {
    System.out.println(object);
    return "Thanks for posting";
  }




}
