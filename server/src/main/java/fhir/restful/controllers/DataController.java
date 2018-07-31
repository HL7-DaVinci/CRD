package fhir.restful.controllers;

import fhir.restful.database.DataRepository;
import fhir.restful.database.Datum;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;



/**
 * Provides the REST interface that can be interacted with at [base]/api/data.
 */
@CrossOrigin(maxAge = 3600)
@RestController
public class DataController {

  @Autowired
  private DataRepository repository;

  public DataController(DataRepository repository) {
    this.repository = repository;

  }

  @GetMapping(value = "/api/data")
  @CrossOrigin(origins = "http://localhost:4200")
  public Collection<Datum> showAll() {
    return repository.findAll();
  }

  /**
   * Gets some data from the repository.
   * @param id the id of the desired data.
   * @return the data from the repository
   */
  @GetMapping("/api/data/{id}")
  public Datum getDatum(@PathVariable long id) {
    Optional<Datum> datum = repository.findById(id);

    if (!datum.isPresent()) {
      throw new DatumNotFoundException();
    }

    return datum.get();
  }

  /**
   * Allows post requests to add data to the repository.
   * @param datum the object to put into the repository
   * @return the response from the server
   */
  @PostMapping("/api/data")
  public ResponseEntity<Object> addDatum(@RequestBody Datum datum) {
    Datum savedDatum = repository.save(datum);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
        .buildAndExpand(savedDatum.getId()).toUri();
    return ResponseEntity.created(location).build();
  }

  @DeleteMapping("/api/data/{id}")
  public long deleteDatum(@PathVariable long id) {
    repository.deleteById(id);
    return id;
  }

  /**
   * Allows updated of data through the REST API.
   * @param datum the new data
   * @param id the id of the data to be replaced
   * @return the response from the server
   */
  @PutMapping("/api/data/{id}")
  public ResponseEntity<Object> updateDatum(@RequestBody Datum datum, @PathVariable long id) {
    Optional<Datum> datumOptional = repository.findById(id);

    if (!datumOptional.isPresent()) {
      return ResponseEntity.notFound().build();
    }
    datum.setId(id);
    repository.save(datum);
    return ResponseEntity.noContent().build();
  }

  @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such datum")  // 404
  public class DatumNotFoundException extends RuntimeException {
    // ...
  }


}
