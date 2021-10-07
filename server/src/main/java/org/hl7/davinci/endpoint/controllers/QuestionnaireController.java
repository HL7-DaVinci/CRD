package org.hl7.davinci.endpoint.controllers;

import org.hl7.davinci.FhirResourceInfo;
import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.Utils;
import org.hl7.davinci.endpoint.database.FhirResource;
import org.hl7.davinci.endpoint.database.FhirResourceRepository;
import org.hl7.davinci.endpoint.files.FileResource;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.davinci.r4.FhirComponents;
import ca.uhn.fhir.parser.IParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemAnswerOptionComponent;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemType;

@CrossOrigin
@RestController
@RequestMapping("/Questionnaire")
public class QuestionnaireController {
    private static Logger logger = Logger.getLogger(Application.class.getName());

    @PostMapping(value = "/$next-question", consumes = { MediaType.APPLICATION_JSON_VALUE, "application/fhir+json" })
    public ResponseEntity<String> retrieveNextQuestion(HttpServletRequest request, HttpEntity<String> entity) {
        return getNextQuestionOperation(entity.getBody(), request);
    }

    private ResponseEntity<String> getNextQuestionOperation(String body, HttpServletRequest request) {
        logger.info("POST /Questionnaire/$next-question fhir+");

        FhirContext ctx = new FhirComponents().getFhirContext();
        IParser parser = ctx.newJsonParser();

        IDomainResource domainResource = (IDomainResource) parser.parseResource(QuestionnaireResponse.class, body);
        if (!domainResource.fhirType().equalsIgnoreCase("QuestionnaireResponse")) {
            logger.warning("unsupported resource type: ");
            HttpStatus status = HttpStatus.BAD_REQUEST;
            MediaType contentType = MediaType.TEXT_PLAIN;
            return ResponseEntity.status(status).contentType(contentType).body("Bad Request");
        } else {
            logger.info(" ---- Resource received " + domainResource.toString());
            QuestionnaireResponse inputResource = (QuestionnaireResponse) domainResource;
            String fragmentId = inputResource.getQuestionnaire();
            List<Resource> containedResource = inputResource.getContained();
            Questionnaire inQuestionnaire = null;
            for (int i = 0; i < containedResource.size(); i++) {
                Resource item = containedResource.get(i);
                if (item.getResourceType().equals(ResourceType.Questionnaire)) {
                    Questionnaire inputQuestionnaire = (Questionnaire) item;
                    if (inputQuestionnaire.getId().equals(fragmentId)) {
                        inQuestionnaire = inputQuestionnaire;
                        break;
                    }
                }
            }

            if (inQuestionnaire != null) {
                // TODO retrieve the questions and set it in the contained Questionnaire
                logger.info("--- Get next question for questionnaire " + inQuestionnaire.getId());
                logger.info("---- Get meta profile " + inQuestionnaire.getMeta().getProfile().get(0).getValue());

                Questionnaire.QuestionnaireItemComponent orderReason = inQuestionnaire.addItem().setLinkId("1");
                orderReason.setText("order Reason");
                orderReason.setType(QuestionnaireItemType.CHOICE);
                orderReason.addAnswerOption(new QuestionnaireItemAnswerOptionComponent()
                        .setValue(new Coding().setCode("Initial or original order for certification")));
                orderReason.addAnswerOption(new QuestionnaireItemAnswerOptionComponent()
                        .setValue(new Coding().setCode("Change in statue")));
                orderReason.addAnswerOption(new QuestionnaireItemAnswerOptionComponent()
                        .setValue(new Coding().setCode("Revision or change in equipment")));
                orderReason.addAnswerOption(
                        new QuestionnaireItemAnswerOptionComponent().setValue(new Coding().setCode("Replacement")));

                List<Resource> newContainedList = new ArrayList<>();
                newContainedList.add(inQuestionnaire);
                // inputResource.setContained(newContainedList);
                inputResource.addContained(inQuestionnaire);
                String formattedResourceString = ctx.newJsonParser().encodeResourceToString(inputResource);

                return ResponseEntity.status(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_TYPE, "application/fhir+json" + "; charset=utf-8")
                        .body(formattedResourceString);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_TYPE, "application/fhir+json" + "; charset=utf-8")
                        .body("Invalid input questionnaire");
            }

        }
    }
}
