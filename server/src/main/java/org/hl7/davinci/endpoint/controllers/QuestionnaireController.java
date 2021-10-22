package org.hl7.davinci.endpoint.controllers;

import org.hl7.davinci.endpoint.Application;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.davinci.r4.FhirComponents;

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemAnswerOptionComponent;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent;
import org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseStatus;

// --- ORDER OF RESPONSE-REQUEST OPERATIONS
// (REQUEST) External user sends the initial QuestionnaireResponse JSON that contains which questionnaire it would like to trigger as n element the "contained" field.
// (RESPONSE) QuestionnaireController adds the first question with its answerResponse options (with its linkId and text) to the JSON in QuestionnaireResponse.contained.item[] and sends it back.
// (REQUEST) External user adds their answer to the question to the JSON in QuestionnaireResponse.item[] and sends it back.
// (RESPONSE) QuestionnaireController takes that response and adds the next indicated question to the JSON in QuestionnaireResponse.contained.item[] and sends it back.
// Repeat intil QuestionnaireController reaches a leaf-node, then it sets the status to "completed" from "in-progress"
// Ultimately, The QuestionnaireController responses add ONLY to the QuestionnaireResponse.contained.item[]. The external requester adds answers to QuestionnaireResponse.item[] and includes the associated linkid and text.

@CrossOrigin
@RestController
@RequestMapping("/Questionnaire")
public class QuestionnaireController {

    /**
     * A class that demos a tree to define next questions based on responses.
     */
    private class AdaptiveQuestionnaireTree {
        private NextQuestionNode root;
    
        /**
         * Constructor.
         * @param inputQuestionnaire    The input questionnaire from the CDS-Library.
         */
        public AdaptiveQuestionnaireTree(Questionnaire inputQuestionnaire) {
            // Build child nodes. (Does not yet include building out children's child nodes. Assumes only one question with followup questions.)
            Map<String, QuestionnaireItemComponent> childMap = new HashMap<String, QuestionnaireItemComponent>();
            // Map of child question IDs for the first question that map to their associated possible response.
            Map<String, String> childIdsToResponses = new HashMap<String, String>();
            // This loop iterates over the possible answer options of the first item in the inputQuestionnaire, which is assumed to be the only parent question.
            for(QuestionnaireItemAnswerOptionComponent answerOption : inputQuestionnaire.getItemFirstRep().getAnswerOption()) {
                // The Id of this answer response's next question.
                String answerNextQuestionId = answerOption.getModifierExtensionFirstRep().getUrl();
                // The response that indicates this answer to the question.
                String possibleAnswerResponse = answerOption.getValueCoding().getCode();
                System.out.println("LLLL:" + answerNextQuestionId);
                System.out.println("AAAA:" + possibleAnswerResponse);
                // Add the key-value pair of next question id to its assocated answer response.
                childIdsToResponses.put(answerNextQuestionId, possibleAnswerResponse);
            }

            // Extract the children and add them to their parent question nodes.
            for(QuestionnaireItemComponent childQuestion : inputQuestionnaire.getItem()){
                String linkId = childQuestion.getLinkId();
                System.out.println("SSSSS:" + linkId);
                System.out.println("QQQQQ:" + childIdsToResponses.keySet());
                if(childIdsToResponses.containsKey(linkId)){
                    // This question is a child of the parent question. Add it to the parent's map of children as a key-value pair of response-childQuestion.
                    childMap.put(childIdsToResponses.get(linkId), childQuestion);
                }
            }
            // Create the root node with its question and children.
            root = new NextQuestionNode(inputQuestionnaire, childMap);
        }

        /**
         * Returns the next question based on the response to the current question.
         * @param response  The response given to this question.
         * @return
         */
        public QuestionnaireItemComponent getNextQuestionForResponse(String response){
            if(!root.children.containsKey(response)){
                throw new NullPointerException("Not a valid response for this question: \'" + response + "\''. Possible responses for this question: \'" + root.children.keySet() + "\''.");
            }
            return root.children.get(response);//.data;
        }

        /**
         * Returns whether this is a leaf node (needs work, but is fine for demo purposes).
         * @param response
         * @return
         */
        public boolean isLeafNode(String response) {
            return !this.getNextQuestionForResponse(response).hasAnswerOption();
        }
    
        /**
         * Inner class that describes a node of the tree.
         */
        private class NextQuestionNode {
            private Questionnaire data; // To be used to contain future answer options for multiple subquestions?
            private Map<String, QuestionnaireItemComponent> children;

            public NextQuestionNode(Questionnaire data, Map<String, QuestionnaireItemComponent> children) {
                this.data = data;
                this.children = children;
            }
        }
    }

    // Logger.
    private static Logger logger = Logger.getLogger(Application.class.getName());
    // Tree that tracks the questions.
    private AdaptiveQuestionnaireTree questionnaireTree;

    /**
     * 
     * @param request
     * @param entity
     * @return
     */
    @PostMapping(value = "/$next-question", consumes = { MediaType.APPLICATION_JSON_VALUE, "application/fhir+json" })
    public ResponseEntity<String> retrieveNextQuestion(HttpServletRequest request, HttpEntity<String> entity) {
        return getNextQuestionOperation(entity.getBody(), request);
    }

    /**
     * 
     * @param body
     * @param request
     * @return
     */
    private ResponseEntity<String> getNextQuestionOperation(String body, HttpServletRequest request) {
        logger.info("POST /Questionnaire/$next-question fhir+");

        FhirContext ctx = new FhirComponents().getFhirContext();
        IParser parser = ctx.newJsonParser();

        // Parses the body.
        IDomainResource domainResource = (IDomainResource) parser.parseResource(QuestionnaireResponse.class, body);
        if (!domainResource.fhirType().equalsIgnoreCase("QuestionnaireResponse")) {
            logger.warning("unsupported resource type: ");
            HttpStatus status = HttpStatus.BAD_REQUEST;
            MediaType contentType = MediaType.TEXT_PLAIN;
            return ResponseEntity.status(status).contentType(contentType).body("Bad Request");
        } else {
            logger.info(" ---- Resource received " + domainResource.toString());
            QuestionnaireResponse inputQuestionnaireResponse = (QuestionnaireResponse) domainResource;
            String fragmentId = inputQuestionnaireResponse.getQuestionnaire();
            List<Resource> containedResource = inputQuestionnaireResponse.getContained();
            Questionnaire inputQuestionnaireFromRequest = null;
            for (int i = 0; i < containedResource.size(); i++) {
                Resource item = containedResource.get(i);
                if (item.getResourceType().equals(ResourceType.Questionnaire)) {
                    Questionnaire checkInputQuestionnaire = (Questionnaire) item;
                    if (checkInputQuestionnaire.getId().equals(fragmentId)) {
                        inputQuestionnaireFromRequest = checkInputQuestionnaire;
                        break;
                    }
                }
            }

            if (inputQuestionnaireFromRequest != null) {

                if(questionnaireTree == null){

                    // Import the requested CDS-Library Questionnaire (Couldn't get CDS to work with it, just reading it in it locally for now. In future will need to be pulled from CDS.)
                    Questionnaire cdsQuestionnaire = null;
                    try {
                        cdsQuestionnaire = (Questionnaire) parser.parseResource(Questionnaire.class, new FileReader(new File("/Users/rscalfani/Documents/code/drlsroot/CRD/server/src/main/java/org/hl7/davinci/endpoint/controllers/Questions-HomeOxygenTherapyAdditional.json")));
                        logger.info("--- Imported Questionnaire " + cdsQuestionnaire.getId());
                    } catch (DataFormatException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if(cdsQuestionnaire == null) {
                        throw new RuntimeException("Requested CDS Questionnaire XXX was not imported and may not exist.");
                    }
                    // Pull the first question from the CDS Questionnaire (because we're assuming that there is only one parent question).
                    QuestionnaireItemComponent currentQuestionItem = cdsQuestionnaire.getItemFirstRep();

                    // Add the first Question item to the contained Questionnaire in the response/request QuestionnaireResponse JSON as part of the response.
                    inputQuestionnaireFromRequest.addItem(currentQuestionItem);

                    // Build the tree and don't expect any answers since we only just received the required questions.
                    questionnaireTree = new AdaptiveQuestionnaireTree(cdsQuestionnaire);
                    System.out.println(inputQuestionnaireFromRequest.getItem());
                    logger.info("--- Built Questionnaire Tree for " + inputQuestionnaireFromRequest.getId());

                } else {
                    // Get the first answer component object from the recieved resource.
                    QuestionnaireResponseItemAnswerComponent answerComponent = inputQuestionnaireResponse.getItem().get(0).getAnswer().get(0);
                    // Pull the string response the person gave.
                    String response = answerComponent.getValueCoding().getCode();
                    // Pull the resulting next question that the recieved response points to from the tree.
                    QuestionnaireItemComponent result = questionnaireTree.getNextQuestionForResponse(response);
                    // Add the next question to the QuestionnaireResponse.contained[0].item[].
                    Questionnaire containedQuestionnaire = (Questionnaire) inputQuestionnaireResponse.getContained().get(0);
                    containedQuestionnaire.addItem(result);
                    logger.info("--- Added next question for questionnaire " + inputQuestionnaireFromRequest.getId() + " for response " + response);
                    // If this question is a leaf node and is the final question, set status to "completed"
                    if(this.questionnaireTree.isLeafNode(response)){
                        inputQuestionnaireResponse.setStatus(QuestionnaireResponseStatus.COMPLETED);
                        logger.info("--- Question leaf node reached, setting status to \"completed\".");
                    }
                }

                logger.info("--- Get next question for questionnaire " + inputQuestionnaireFromRequest.getId());
                logger.info("---- Get meta profile " + inputQuestionnaireFromRequest.getMeta().getProfile().get(0).getValue());
                
                // Build and send the response.
                String formattedResourceString = ctx.newJsonParser().encodeResourceToString(inputQuestionnaireResponse);
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