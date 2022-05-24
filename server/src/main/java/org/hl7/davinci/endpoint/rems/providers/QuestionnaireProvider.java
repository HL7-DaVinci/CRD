package org.hl7.davinci.endpoint.rems.providers;

import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import org.hl7.davinci.endpoint.rems.database.fhir.RemsFhir;
import org.hl7.davinci.endpoint.rems.database.fhir.RemsFhirRepository;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class QuestionnaireProvider implements IResourceProvider {

    @Autowired
    RemsFhirRepository remsFhirRepository;

    IParser jsonParser;

    /**
     * Constructor
     */
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
        return (Questionnaire) jsonParser.parseResource(retVal.getResource().toString());
    }

    @Create
    public MethodOutcome createQuestionnaire(@ResourceParam Questionnaire theQuestionnaire) {
        RemsFhir resource = new RemsFhir();
        resource.setResourceType(ResourceType.Questionnaire.toString());
        String uuid = UUID.randomUUID().toString();
        theQuestionnaire.setId(uuid);
        resource.setId(uuid);
        resource.setResource(JacksonUtil.toJsonNode(jsonParser.encodeResourceToString(theQuestionnaire)));
        remsFhirRepository.save(resource);
        MethodOutcome methodOutcome = new MethodOutcome();
        methodOutcome.setResource(theQuestionnaire);
        return methodOutcome;
    }

}