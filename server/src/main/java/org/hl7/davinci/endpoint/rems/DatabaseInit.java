package org.hl7.davinci.endpoint.rems;

import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import org.hl7.davinci.endpoint.rems.database.drugs.Drug;
import org.hl7.davinci.endpoint.rems.database.drugs.DrugsRepository;
import org.hl7.davinci.endpoint.rems.database.fhir.RemsFhir;
import org.hl7.davinci.endpoint.rems.database.fhir.RemsFhirRepository;
import org.hl7.davinci.endpoint.rems.database.requirement.Requirement;
import org.hl7.davinci.endpoint.rems.database.requirement.RequirementRepository;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
class DatabaseInit {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInit.class);

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        System.out.println(Paths.get(path).toAbsolutePath());

        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }


    @Bean
    CommandLineRunner initDatabase(DrugsRepository repository, RemsFhirRepository remsFhirRepository, RequirementRepository requirementRepository) {
        FhirComponents fhirComponents = new FhirComponents();
        IParser jsonParser = fhirComponents.getJsonParser();

        return args -> {
            log.info("Preloading turalio");
            Drug turalio = new Drug();

            String questionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Turalio/fhir/Questionnaire-R4-DrugHasREMS.json", Charset.defaultCharset());
            Requirement requirement = new Requirement();
            RemsFhir remsFhir = new RemsFhir();
            remsFhir.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode questionnaireResource = JacksonUtil.toJsonNode(questionnaire);
            remsFhir.setResource(questionnaireResource);
            remsFhir.setId("q1");
            remsFhirRepository.save(remsFhir);
            requirement.setRequirement(remsFhir);
            requirement.setDescription("complete questionnaire");
            turalio.addRequirement(requirement);
            turalio.setId("turalio");
            repository.save(turalio);
            requirement.setDrug(turalio);
            requirementRepository.save(requirement);
        };
    }
}