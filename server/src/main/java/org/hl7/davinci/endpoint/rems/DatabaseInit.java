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
            Drug turalio = new Drug(), tirf = new Drug();
            turalio.setId("turalio");
            turalio.setCodeSystem("http://www.nlm.nih.gov/research/umls/rxnorm");
            turalio.setCode("2183126");
            repository.save(turalio);;

            tirf.setId("TIRF");
            tirf.setCodeSystem("http://www.nlm.nih.gov/research/umls/rxnorm");
            tirf.setCode("1237051");
            repository.save(tirf);


            /*-------------------------------------------------- TURALIO --------------------------------------------------*/

            
            // patient enrollment form requirement
            String TuralioPatientQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Turalio/fhir/Questionnaire-R4-DrugHasREMS.json", Charset.defaultCharset());
            Requirement TuralioPatientEnrollmentRequirement = new Requirement();
            RemsFhir TuralioPatientEnrollmentResource = new RemsFhir();
            TuralioPatientEnrollmentResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode TuralioPatientQuestionnaireResource = JacksonUtil.toJsonNode(TuralioPatientQuestionnaire);
            TuralioPatientEnrollmentResource.setResource(TuralioPatientQuestionnaireResource);
            TuralioPatientEnrollmentResource.setId("TuralioRemsPatientEnrollment");
            remsFhirRepository.save(TuralioPatientEnrollmentResource);
            TuralioPatientEnrollmentRequirement.setName("Patient Enrollment");
            TuralioPatientEnrollmentRequirement.setCreateNewCase(true);
            TuralioPatientEnrollmentRequirement.setResource(TuralioPatientEnrollmentResource);
            TuralioPatientEnrollmentRequirement.setDescription("Submit Patient Enrollment form to the REMS Administrator");
            TuralioPatientEnrollmentRequirement.setDrug(turalio);
            TuralioPatientEnrollmentRequirement.setStakeholder("patient");
            requirementRepository.save(TuralioPatientEnrollmentRequirement);

             // prescriber enrollment form requirement
             String TuralioPrescriberQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Turalio/Questionnaire-R4-Prescriber-Enrollment.json", Charset.defaultCharset());
             Requirement TuralioPrescriberEnrollmentRequirement = new Requirement();
             RemsFhir TuralioPrescriberEnrollmentResource = new RemsFhir();
             TuralioPrescriberEnrollmentResource.setResourceType(ResourceType.Questionnaire.toString());
             JsonNode TuralioPrescriberQuestionnaireResource = JacksonUtil.toJsonNode(TuralioPrescriberQuestionnaire);
             TuralioPrescriberEnrollmentResource.setResource(TuralioPrescriberQuestionnaireResource);
             TuralioPrescriberEnrollmentResource.setId("TuralioPrescriberEnrollmentForm");
             remsFhirRepository.save(TuralioPrescriberEnrollmentResource);
             TuralioPrescriberEnrollmentRequirement.setName("Prescriber Enrollment");
             TuralioPrescriberEnrollmentRequirement.setCreateNewCase(false);
             TuralioPrescriberEnrollmentRequirement.setResource(TuralioPrescriberEnrollmentResource);
             TuralioPrescriberEnrollmentRequirement.setDescription("Submit Prescriber Enrollment form and training certification to the REMS Administrator");
             TuralioPrescriberEnrollmentRequirement.setDrug(turalio);
             TuralioPrescriberEnrollmentRequirement.setStakeholder("prescriber");
             requirementRepository.save(TuralioPrescriberEnrollmentRequirement);

            // prescriber knowledge assessment / certification sub-requirement
            String TuralioPrescriberKnowledgeQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Turalio/Questionnaire-R4-Prescriber-Knowledge-Assessment.json", Charset.defaultCharset());
            Requirement TuralioPrescriberCertificationRequirement = new Requirement();
            RemsFhir TuralioPrescriberKnowledgeResource = new RemsFhir();
            TuralioPrescriberKnowledgeResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode TuralioPrescriberKnowledgeQuestionnaireResource = JacksonUtil.toJsonNode(TuralioPrescriberKnowledgeQuestionnaire);
            TuralioPrescriberKnowledgeResource.setResource(TuralioPrescriberKnowledgeQuestionnaireResource);
            TuralioPrescriberKnowledgeResource.setId("TuralioPrescriberKnowledgeAssessment");
            remsFhirRepository.save(TuralioPrescriberKnowledgeResource);
            TuralioPrescriberCertificationRequirement.setName("Prescriber Knowledge Assessment");
            TuralioPrescriberCertificationRequirement.setCreateNewCase(false);
            TuralioPrescriberCertificationRequirement.setResource(TuralioPrescriberKnowledgeResource);
            TuralioPrescriberCertificationRequirement.setDescription("Submit Prescriber Knowledge Assessment Form to REMS Administrator to receive certification");
            TuralioPrescriberCertificationRequirement.setParentRequirement(TuralioPrescriberEnrollmentRequirement);
            TuralioPrescriberCertificationRequirement.setStakeholder("prescriber");
            // prescriberCertificationRequirement.setDrug(turalio); 
            requirementRepository.save(TuralioPrescriberCertificationRequirement);

             // pharmacist enrollment form requirement
             // change form below to pharmacist enrollment once form is translated
             String TuralioPharmacistQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Turalio/Questionnaire-R4-Prescriber-Enrollment.json", Charset.defaultCharset());
             Requirement TuralioPharmacistEnrollmentRequirement = new Requirement();
             RemsFhir TuralioPharmacistEnrollmentResource = new RemsFhir();
             TuralioPharmacistEnrollmentResource.setResourceType(ResourceType.Questionnaire.toString());
             JsonNode TuralioPharmacistQuestionnaireResource = JacksonUtil.toJsonNode(TuralioPharmacistQuestionnaire);
             TuralioPharmacistEnrollmentResource.setResource(TuralioPharmacistQuestionnaireResource);
             TuralioPharmacistEnrollmentResource.setId("TuralioPharmacistEnrollment");
             remsFhirRepository.save(TuralioPharmacistEnrollmentResource);
             TuralioPharmacistEnrollmentRequirement.setName("Pharmacist Enrollment");
             TuralioPharmacistEnrollmentRequirement.setCreateNewCase(false);
             TuralioPharmacistEnrollmentRequirement.setResource(TuralioPharmacistEnrollmentResource);
             TuralioPharmacistEnrollmentRequirement.setDescription("Submit Pharmacist Enrollment form and training certification to the REMS Administrator");
             TuralioPharmacistEnrollmentRequirement.setDrug(turalio);
             TuralioPharmacistEnrollmentRequirement.setStakeholder("pharmacist");
             requirementRepository.save(TuralioPharmacistEnrollmentRequirement);


            /*-------------------------------------------------- TIRF --------------------------------------------------*/


            // patient enrollment form requirement
            String TIRFPatientQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/TIRF/Questionnaire-R4-PrescriberEnrollment-TIRF.json", Charset.defaultCharset());
            Requirement TIRFPatientEnrollmentRequirement = new Requirement();
            RemsFhir TIRFPatientEnrollmentResource = new RemsFhir();
            TIRFPatientEnrollmentResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode TIRFPatientQuestionnaireResource = JacksonUtil.toJsonNode(TIRFPatientQuestionnaire);
            TIRFPatientEnrollmentResource.setResource(TIRFPatientQuestionnaireResource);
            TIRFPatientEnrollmentResource.setId("TIRFRemsPatientEnrollment");
            remsFhirRepository.save(TIRFPatientEnrollmentResource);
            TIRFPatientEnrollmentRequirement.setName("Patient Enrollment");
            TIRFPatientEnrollmentRequirement.setCreateNewCase(true);
            TIRFPatientEnrollmentRequirement.setResource(TIRFPatientEnrollmentResource);
            TIRFPatientEnrollmentRequirement.setDescription("Submit Patient Enrollment form to the REMS Administrator");
            TIRFPatientEnrollmentRequirement.setDrug(tirf);
            TIRFPatientEnrollmentRequirement.setStakeholder("patient");
            requirementRepository.save(TIRFPatientEnrollmentRequirement);

            // prescriber enrollment form requirement
            String TIRFPrescriberQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/TIRF/Questionnaire-R4-PrescriberEnrollment-TIRF.json", Charset.defaultCharset());
            Requirement TIRFPrescriberEnrollmentRequirement = new Requirement();
            RemsFhir TIRFPrescriberEnrollmentResource = new RemsFhir();
            TIRFPrescriberEnrollmentResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode TIRFPrescriberQuestionnaireResource = JacksonUtil.toJsonNode(TIRFPrescriberQuestionnaire);
            TIRFPrescriberEnrollmentResource.setResource(TIRFPrescriberQuestionnaireResource);
            TIRFPrescriberEnrollmentResource.setId("TIRFPrescriberEnrollmentForm");
            remsFhirRepository.save(TIRFPrescriberEnrollmentResource);
            TIRFPrescriberEnrollmentRequirement.setName("Prescriber Enrollment");
            TIRFPrescriberEnrollmentRequirement.setCreateNewCase(false);
            TIRFPrescriberEnrollmentRequirement.setResource(TIRFPrescriberEnrollmentResource);
            TIRFPrescriberEnrollmentRequirement.setDescription("Submit Prescriber Enrollment form to the REMS Administrator");
            TIRFPrescriberEnrollmentRequirement.setDrug(tirf);
            TIRFPrescriberEnrollmentRequirement.setStakeholder("prescriber");
            requirementRepository.save(TIRFPrescriberEnrollmentRequirement);

            // prescriber knowledge assessment / certification sub-requirement
            String TIRFPrescriberKnowledgeQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/TIRF/Questionnaire-R4-Prescriber-Knowledge-Assessment-TIRF.json", Charset.defaultCharset());
            Requirement TIRFPrescriberCertificationRequirement = new Requirement();
            RemsFhir TIRFPrescriberKnowledgeResource = new RemsFhir();
            TIRFPrescriberKnowledgeResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode TIRFPrescriberKnowledgeQuestionnaireResource = JacksonUtil.toJsonNode(TIRFPrescriberKnowledgeQuestionnaire);
            TIRFPrescriberKnowledgeResource.setResource(TIRFPrescriberKnowledgeQuestionnaireResource);
            TIRFPrescriberKnowledgeResource.setId("TIRFPrescriberKnowledgeAssessment");
            remsFhirRepository.save(TIRFPrescriberKnowledgeResource);
            TIRFPrescriberCertificationRequirement.setName("Prescriber Knowledge Assessment");
            TIRFPrescriberCertificationRequirement.setCreateNewCase(false);
            TIRFPrescriberCertificationRequirement.setResource(TIRFPrescriberKnowledgeResource);
            TIRFPrescriberCertificationRequirement.setDescription("Submit Prescriber Knowledge Assessment form to the REMS Administrator to receive certification");
            TIRFPrescriberCertificationRequirement.setParentRequirement(TIRFPrescriberEnrollmentRequirement);
            TIRFPrescriberCertificationRequirement.setStakeholder("prescriber");
            //TIRFPrescriberCertificationRequirement.setDrug(tirf);
            requirementRepository.save(TIRFPrescriberCertificationRequirement);

            // pharmacist enrollment form requirement
            // change form below to pharmacist enrollment once form is translated
            String TIRFPharmacistQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/TIRF/Questionnaire-R4-PrescriberEnrollment-TIRF.json", Charset.defaultCharset());
            Requirement TIRFPharmacistEnrollmentRequirement = new Requirement();
            RemsFhir TIRFPharmacistEnrollmentResource = new RemsFhir();
            TIRFPharmacistEnrollmentResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode TIRFPharmacistQuestionnaireResource = JacksonUtil.toJsonNode(TIRFPharmacistQuestionnaire);
            TIRFPharmacistEnrollmentResource.setResource(TIRFPharmacistQuestionnaireResource);
            TIRFPharmacistEnrollmentResource.setId("TIRFPharmacistEnrollmentForm");
            remsFhirRepository.save(TIRFPharmacistEnrollmentResource);
            TIRFPharmacistEnrollmentRequirement.setName("Pharmacist Enrollment");
            TIRFPharmacistEnrollmentRequirement.setCreateNewCase(false);
            TIRFPharmacistEnrollmentRequirement.setResource(TIRFPharmacistEnrollmentResource);
            TIRFPharmacistEnrollmentRequirement.setDescription("Submit Pharmacist Enrollment form to the REMS Administrator");
            TIRFPharmacistEnrollmentRequirement.setDrug(tirf);
            TIRFPharmacistEnrollmentRequirement.setStakeholder("pharmacist");
            requirementRepository.save(TIRFPharmacistEnrollmentRequirement);

            // pharmacist knowledge assessment / certification sub-requirement
            // change form below to pharmacist enrollment once form is translated
            String TIRFPharmacistKnowledgeQuestionnaire = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/TIRF/Questionnaire-R4-PrescriberEnrollment-TIRF.json", Charset.defaultCharset());
            Requirement TIRFPharmacistCertificationRequirement = new Requirement();
            RemsFhir TIRFPharmacistKnowledgeResource = new RemsFhir();
            TIRFPharmacistKnowledgeResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode TIRFPharmacistKnowledgeQuestionnaireResource = JacksonUtil.toJsonNode(TIRFPharmacistKnowledgeQuestionnaire);
            TIRFPharmacistKnowledgeResource.setResource(TIRFPharmacistKnowledgeQuestionnaireResource);
            TIRFPharmacistKnowledgeResource.setId("TIRFPharmacistKnowledgeAssessment");
            remsFhirRepository.save(TIRFPharmacistKnowledgeResource);
            TIRFPharmacistCertificationRequirement.setName("Pharmacist Knowledge Assessment");
            TIRFPharmacistCertificationRequirement.setCreateNewCase(false);
            TIRFPharmacistCertificationRequirement.setResource(TIRFPharmacistKnowledgeResource);
            TIRFPharmacistCertificationRequirement.setDescription("Submit Pharmacist Knowledge Assessment form to the REMS Administrator to receive certification");
            TIRFPharmacistCertificationRequirement.setParentRequirement(TIRFPharmacistEnrollmentRequirement);
            TIRFPharmacistCertificationRequirement.setStakeholder("pharmacist");
            //TIRFPharmacistCertificationRequirement.setDrug(tirf);
            requirementRepository.save(TIRFPharmacistCertificationRequirement);



            /*########################################################## MET REQUIREMENTS ##########################################################*/



            /*-------------------------------------------------- TURALIO --------------------------------------------------*/


            // pharmacist enrollment form requirement
            String TuralioPharmacistOrganization = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Pharmacist-Organization.json", Charset.defaultCharset());
            MetRequirement TuralioPharmacistEnrollmentMetRequirement = new MetRequirement();
            RemsFhir TuralioPharmacistCredentialsResource = new RemsFhir();
            TuralioPharmacistCredentialsResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode TuralioPharmacistOrganizationResource = JacksonUtil.toJsonNode(TuralioPharmacistOrganization);
            TuralioPharmacistCredentialsResource.setResource(TuralioPharmacistOrganizationResource);
            TuralioPharmacistCredentialsResource.setId("Turalio-pharmacist-organization");
            remsFhirRepository.save(TuralioPharmacistCredentialsResource);
            TuralioPharmacistEnrollmentMetRequirement.setCompleted(true);
            String turalioFunctionalId = TuralioPharmacistOrganizationResource.get("resourceType").textValue() + "/" + TuralioPharmacistOrganizationResource.get("id").textValue();
            TuralioPharmacistEnrollmentMetRequirement.setFunctionalId(turalioFunctionalId);
            TuralioPharmacistEnrollmentMetRequirement.setRequirement(TuralioPharmacistEnrollmentRequirement);
            TuralioPharmacistEnrollmentMetRequirement.setCompletedRequirement(TuralioPharmacistCredentialsResource);
            metRequirementRepository.save(TuralioPharmacistEnrollmentMetRequirement);


            /*-------------------------------------------------- TIRF --------------------------------------------------*/
            

            // pharmacist enrollment assessment
            String TIRFPharmacistOrganization = readFile("src/main/java/org/hl7/davinci/endpoint/rems/resources/Pharmacist-Organization.json", Charset.defaultCharset());
            MetRequirement TIRFPharmacistEnrollmentMetRequirement = new MetRequirement();
            RemsFhir TIRFPharmacistCredentialsResource = new RemsFhir();
            TIRFPharmacistCredentialsResource.setResourceType(ResourceType.Questionnaire.toString());
            JsonNode TIRFPharmacistOrganizationResource = JacksonUtil.toJsonNode(TIRFPharmacistOrganization);
            TIRFPharmacistCredentialsResource.setResource(TIRFPharmacistOrganizationResource);
            TIRFPharmacistCredentialsResource.setId("TIRF-pharmacist-organization");
            remsFhirRepository.save(TIRFPharmacistCredentialsResource);
            TIRFPharmacistEnrollmentMetRequirement.setCompleted(true);
            String TIRFFunctionalId = TIRFPharmacistOrganizationResource.get("resourceType").textValue() + "/" + TIRFPharmacistOrganizationResource.get("id").textValue();
            TIRFPharmacistEnrollmentMetRequirement.setFunctionalId(TIRFFunctionalId);
            TIRFPharmacistEnrollmentMetRequirement.setRequirement(TIRFPharmacistEnrollmentRequirement);
            TIRFPharmacistEnrollmentMetRequirement.setCompletedRequirement(TIRFPharmacistCredentialsResource);
            metRequirementRepository.save(TIRFPharmacistEnrollmentMetRequirement);

            // pharmacist knowledge form requirement
            MetRequirement TIRFPharmacistCertificationMetRequirement = new MetRequirement();
            TIRFPharmacistCertificationMetRequirement.setCompleted(true);
            TIRFPharmacistCertificationMetRequirement.setFunctionalId(TIRFFunctionalId);
            TIRFPharmacistCertificationMetRequirement.setRequirement(TIRFPharmacistCertificationRequirement);
            TIRFPharmacistCertificationMetRequirement.setParentMetRequirement(TIRFPharmacistEnrollmentMetRequirement);
            metRequirementRepository.save(TIRFPharmacistCertificationMetRequirement);

        };
    }
}