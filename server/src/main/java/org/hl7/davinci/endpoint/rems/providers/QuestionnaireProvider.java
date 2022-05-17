package org.hl7.davinci.endpoint.rems.providers;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.davinci.endpoint.rems.database.fhir.RemsFhir;
import org.hl7.davinci.endpoint.rems.database.fhir.RemsFhirRepository;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class QuestionnaireProvider implements IResourceProvider {
    /**
     * Constructor
     */

    @Autowired
    RemsFhirRepository remsFhirRepository;

    IParser jsonParser;

    public QuestionnaireProvider() {
        FhirComponents fhirComponents = new FhirComponents();
        jsonParser = fhirComponents.getJsonParser();
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Questionnaire.class;
    }

    /**
     * Simple implementation of the "read" method
     */
    @Read()
    public Questionnaire read(@IdParam IdType theId) {
        RemsFhir retVal = remsFhirRepository.findById(theId.getIdPart()).get();
        if (retVal == null) {
            throw new ResourceNotFoundException(theId);
        }
        return (Questionnaire) jsonParser.parseResource(retVal.getResource());
    }

    @Create
    public MethodOutcome createQuestionnaire(@ResourceParam Questionnaire theQuestionnaire) {
        RemsFhir resource = new RemsFhir();
        resource.setResourceType(ResourceType.Questionnaire.toString());
        String uuid = UUID.randomUUID().toString();
        theQuestionnaire.setId(uuid);
        resource.setId(uuid);
        resource.setResource(jsonParser.encodeResourceToString(theQuestionnaire));
        remsFhirRepository.save(resource);
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setResource(theQuestionnaire);
        return methodOutcome;
    }

}