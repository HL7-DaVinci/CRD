package fhir.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class DataController {

    @Autowired
    private DataRepository repository;

    public DataController(DataRepository repository){
        this.repository = repository;

    }

    @GetMapping("/data")
    @CrossOrigin(origins = "http://localhost:4200")
    public Collection<Datum> showAll(){
        return repository.findAll();
    }

    @GetMapping("/data/{id}")
    public Datum getDatum(@PathVariable long id){
        Optional<Datum> datum = repository.findById(id);

        if (!datum.isPresent())
            throw new DatumNotFoundException();

        return datum.get();
    }

    @PostMapping("/data")
    public ResponseEntity<Object> addDatum(@RequestBody Datum datum){
        Datum savedDatum = repository.save(datum);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedDatum.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No such datum")  // 404
    public class DatumNotFoundException extends RuntimeException {
        // ...
    }
}
