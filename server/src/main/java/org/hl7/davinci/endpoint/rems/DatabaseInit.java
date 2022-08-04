package org.hl7.davinci.endpoint.rems;

import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;
import org.hl7.davinci.endpoint.rems.database.drugs.Drug;
import org.hl7.davinci.endpoint.rems.database.drugs.DrugsRepository;
import org.hl7.davinci.endpoint.rems.database.fhir.RemsFhir;
import org.hl7.davinci.endpoint.rems.database.fhir.RemsFhirRepository;
import org.hl7.davinci.endpoint.rems.database.requirement.Requirement;
import org.hl7.davinci.endpoint.rems.database.requirement.MetRequirement;
import org.hl7.davinci.endpoint.rems.database.requirement.RequirementRepository;
import org.hl7.davinci.endpoint.rems.database.requirement.MetRequirementRepository;
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
    CommandLineRunner initDatabase(DrugsRepository repository, RemsFhirRepository remsFhirRepository, RequirementRepository requirementRepository, MetRequirementRepository metRequirementRepository) {
        FhirComponents fhirComponents = new FhirComponents();
        IParser jsonParser = fhirComponents.getJsonParser();

        return args -> {
            log.info("Preloading turalio");
            Drug turalio = new Drug();
            turalio.setId("turalio");
            turalio.setCodeSystem("http://www.nlm.nih.gov/research/umls/rxnorm");
            turalio.setCode("2183126");
            repository.save(turalio);;


            // patient enrollment form requirement
            String patientQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Turalio/fhir/Questionnaire-R4-DrugHasREMS.json", Charset.defaultCharset());
            Requirement patientEnrollmentRequirement = new Requirement();
            RemsFhir patientEnrollmentResource = new RemsFhir();
            patientEnrollmentResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode patientQuestionnaireResource = JacksonUtil.toJsonNode(patientQuestionnaire);
            patientEnrollmentResource.setResource(patientQuestionnaireResource);
            patientEnrollmentResource.setId("turalio-patient-enrollment");
            remsFhirRepository.save(patientEnrollmentResource);
            patientEnrollmentRequirement.setName("Patient Enrollment");
            patientEnrollmentRequirement.setResource(patientEnrollmentResource);
            patientEnrollmentRequirement.setDescription("Submit Patient Enrollment form to the REMS Administrator");
            patientEnrollmentRequirement.setDrug(turalio);
            requirementRepository.save(patientEnrollmentRequirement);

             // prescriber enrollment form requirement
             String prescriberQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Turalio/Questionnaire-R4-Prescriber-Enrollment.json", Charset.defaultCharset());
             Requirement prescriberEnrollmentRequirement = new Requirement();
             RemsFhir prescriberEnrollmentResource = new RemsFhir();
             prescriberEnrollmentResource.setResourceType(ResourceType.Questionnaire.toString());
             JsonNode prescriberQuestionnaireResource = JacksonUtil.toJsonNode(prescriberQuestionnaire);
             prescriberEnrollmentResource.setResource(prescriberQuestionnaireResource);
             prescriberEnrollmentResource.setId("turalio-prescriber-enrollment");
             remsFhirRepository.save(prescriberEnrollmentResource);
             prescriberEnrollmentRequirement.setName("Prescriber Enrollment");
             prescriberEnrollmentRequirement.setResource(prescriberEnrollmentResource);
             prescriberEnrollmentRequirement.setDescription("Submit Prescriber Enrollment form and training certification to the REMS Administrator");
             prescriberEnrollmentRequirement.setDrug(turalio);
             requirementRepository.save(prescriberEnrollmentRequirement);

            // prescriber knowledge assessment / certification sub-requirement
            String prescriberKnowledgeQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Turalio/Questionnaire-R4-Prescriber-Knowledge-Assessment.json", Charset.defaultCharset());
            Requirement prescriberCertificationRequirement = new Requirement();
            RemsFhir prescriberKnowledgeResource = new RemsFhir();
            prescriberKnowledgeResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode prescriberKnowledgeQuestionnaireResource = JacksonUtil.toJsonNode(prescriberKnowledgeQuestionnaire);
            prescriberKnowledgeResource.setResource(prescriberKnowledgeQuestionnaireResource);
            prescriberKnowledgeResource.setId("turalio-prescriber-knowledge-check");
            remsFhirRepository.save(prescriberKnowledgeResource);
            prescriberCertificationRequirement.setName("Prescriber Knowledge Assessment");
            prescriberCertificationRequirement.setResource(prescriberKnowledgeResource);
            prescriberCertificationRequirement.setDescription("Submit Prescriber Knowledge Assessment Form to REMS Administrator to receive certification");
            prescriberCertificationRequirement.setParentRequirement(prescriberEnrollmentRequirement);
            // prescriberCertificationRequirement.setDrug(turalio); 
            requirementRepository.save(prescriberCertificationRequirement);

             // pharmacist enrollment form requirement
             // change form below to pharmacist enrollment once form is translated
             String pharmacistQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Turalio/Questionnaire-R4-Prescriber-Enrollment.json", Charset.defaultCharset());
             Requirement pharmacistEnrollmentRequirement = new Requirement();
             RemsFhir pharmacistEnrollmentResource = new RemsFhir();
             pharmacistEnrollmentResource.setResourceType(ResourceType.Questionnaire.toString());
             JsonNode pharmacistQuestionnaireResource = JacksonUtil.toJsonNode(pharmacistQuestionnaire);
             pharmacistEnrollmentResource.setResource(pharmacistQuestionnaireResource);
             pharmacistEnrollmentResource.setId("turalio-pharmacist-enrollment");
             remsFhirRepository.save(pharmacistEnrollmentResource);
             pharmacistEnrollmentRequirement.setName("Pharmacist Enrollment");
             pharmacistEnrollmentRequirement.setResource(pharmacistEnrollmentResource);
             pharmacistEnrollmentRequirement.setDescription("Submit Pharmacist Enrollment form and training certification to the REMS Administrator");
             pharmacistEnrollmentRequirement.setDrug(turalio);
             requirementRepository.save(pharmacistEnrollmentRequirement);

            /* pharmacist knowledge assessment / certification sub-requirement
            // change form below to pharmacist knowledge assessment once form is translated
            String pharmacistKnowledgeQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Turalio/Questionnaire-R4-Prescriber-Knowledge-Assessment.json", Charset.defaultCharset());
            Requirement pharmacistCertificationRequirement = new Requirement();
            RemsFhir pharmacistKnowledgeResource = new RemsFhir();
            pharmacistKnowledgeResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode pharmacistKnowledgeQuestionnaireResource = JacksonUtil.toJsonNode(pharmacistKnowledgeQuestionnaire);
            pharmacistKnowledgeResource.setResource(pharmacistKnowledgeQuestionnaireResource);
            pharmacistKnowledgeResource.setId("turalio-pharmacist-knowledge-check");
            remsFhirRepository.save(pharmacistKnowledgeResource);
            pharmacistCertificationRequirement.setName("Pharmacist Knowledge Assessment");
            pharmacistCertificationRequirement.setResource(pharmacistKnowledgeResource);
            pharmacistCertificationRequirement.setDescription("Submit Pharmacist Knowledge Assessment Form to REMS Administrator to receive certification");
            pharmacistCertificationRequirement.setParentRequirement(pharmacistEnrollmentRequirement);
            // pharmacistCertificationRequirement.setDrug(turalio); 
            requirementRepository.save(pharmacistCertificationRequirement); */


            /*-------------------------------------------------- MET REQUIREMENTS --------------------------------------------------*/


            // pharmacist knowledge assessment / certification sub-requirement
            // change form below to pharmacist knowledge assessment once form is translated
            String pharmacistOrganization = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Turalio/Pharmacist-Organization.json", Charset.defaultCharset());
            MetRequirement pharmacistEnrollmentMetRequirement = new MetRequirement();
            RemsFhir pharmacistCredentialsResource = new RemsFhir();
            pharmacistCredentialsResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode pharmacistOrganizationResource = JacksonUtil.toJsonNode(pharmacistOrganization);
            pharmacistCredentialsResource.setResource(pharmacistOrganizationResource);
            pharmacistCredentialsResource.setId("turalio-pharmacist-organization");
            remsFhirRepository.save(pharmacistCredentialsResource);
            pharmacistEnrollmentMetRequirement.setCompleted(true);
            pharmacistEnrollmentMetRequirement.setRequirement(pharmacistEnrollmentRequirement);
            pharmacistEnrollmentMetRequirement.setCompletedRequirement(pharmacistCredentialsResource);
            metRequirementRepository.save(pharmacistEnrollmentMetRequirement);


        };
    }
}