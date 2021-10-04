package org.hl7.davinci.endpoint.controllers;

import org.hl7.davinci.FhirResourceInfo;
import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.Utils;
import org.hl7.davinci.endpoint.database.FhirResource;
import org.hl7.davinci.endpoint.database.FhirResourceRepository;
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

@CrossOrigin
@RestController
@RequestMapping("/Questionnaire")
public class QuestionnaireController {
    private static Logger logger = Logger.getLogger(Application.class.getName());

    @PostMapping(value = "/$next-question", consumes = { MediaType.APPLICATION_JSON_VALUE, "application/fhir+json" })
    public ResponseEntity<String> retrieveNextQuestion(HttpServletRequest request, HttpEntity<String> entity) {
        return getNextQuestionOperation(entity.getBody(), request);
    }

    private ResponseEntity<String> getNextQuestionOperation(String body,  HttpServletRequest request) {
        logger.info("POST /Questionnaire/$next-question fhir+" );
        return ResponseEntity.status(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.CONTENT_TYPE, "application/fhir+json" + "; charset=utf-8")
        .body("Request received");
    }
}
