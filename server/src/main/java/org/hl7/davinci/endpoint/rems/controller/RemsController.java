package org.hl7.davinci.endpoint.rems.controller;

import javassist.NotFoundException;
import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.files.FileResource;
import org.hl7.davinci.endpoint.rems.database.drugs.Drug;
import org.hl7.davinci.endpoint.rems.database.drugs.DrugsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Provides the REST interface that can be interacted with at [base]/api/fhir and [base]/fhir.
 */
@RestController
public class RemsController {
    private static Logger logger = Logger.getLogger(Application.class.getName());

    @Autowired
    private DrugsRepository drugsRepository;

    @GetMapping(value = "/rems/{id}")
    @CrossOrigin
    public ResponseEntity<Drug> getRequirments(HttpServletRequest request, @PathVariable String id) throws IOException {
        Drug drug = drugsRepository.findById(id).get();
        return processRequirements(drug);
    }

    private ResponseEntity<Drug> processRequirements(Drug drug) {
        if (drug == null) {
            logger.warning("drug not found, return error (404)");
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_JSON_VALUE))
                .body(drug);
    }
}