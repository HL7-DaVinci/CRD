package org.hl7.davinci.endpoint.controllers;

import org.hl7.davinci.endpoint.Application;
import org.hl7.davinci.endpoint.files.FileResource;
import org.hl7.davinci.endpoint.files.FileStore;
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

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemAnswerOptionComponent;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent;
import org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseItemComponent;
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


    @Autowired
    private FileStore fileStore;

    /**
     * An inner class that demos a tree to define next questions based on responses.
     */
    private class AdaptiveQuestionnaireTree {
        
        // The current question (defiend within the node).
        private AdaptiveQuestionnaireNode root;
    
        /**
         * Initial constructor that generates the beginning of the tree.
         * @param inputQuestionnaire    The input questionnaire from the CDS-Library.
         */
        public AdaptiveQuestionnaireTree(Questionnaire inputQuestionnaire) {

            // Because of the nested structure of the input JSON, there can be only one super-parent questionitem in the inputQuestionnaire.
            if(inputQuestionnaire.getItem().size() != 1){
                throw new RuntimeException("An input Adaptive next-question questionnaire can have only one super-parent question.item, found " + inputQuestionnaire.getItem().size() + ".");
            }

            // Top level parent question item. This is also the first question.
            QuestionnaireItemComponent topQuestion = inputQuestionnaire.getItemFirstRep();

            // Start the root building.
            this.root = new AdaptiveQuestionnaireNode(topQuestion);
        }

        /**
         * Returns the next question based on the response to the current question. Also sets the next question based on that response.
         * @param response  The response given to this question.
         * @return
         */
        public QuestionnaireItemComponent getNextQuestionForResponse(String response){
            if(!root.children.containsKey(response)){
                throw new RuntimeException("Not a valid response for question: \'" + this.root.questionItem.getText() + "\' with response \'" + response + "\'. Possible responses for this question: \'" + root.children.keySet() + "\'.");
            }
            // Pull the current question.
            QuestionnaireItemComponent currentQuestionnaireItem = this.root.getChildForResponse(response).questionItem;
            // Set the new next question.
            this.root = this.root.getChildForResponse(response);
            // Return the prior current question.
            return currentQuestionnaireItem;
        }

        /**
         * Returns whether this has reached a leaf node.
         * @param response
         * @return
         */
        public boolean reachedLeafNode() {
            return this.root.isLeafNode();
        }

        /**
         * Returns the linkid of the current question.
         * @return
         */
        public String getCurrentQuestionId() {
            return this.root.getQuestionId();
        }
    
        /**
         * Inner class that describes a node of the tree.
         */
        private class AdaptiveQuestionnaireNode {

            // Contains the current question item of the node.
            private QuestionnaireItemComponent questionItem;
            // Map of (answerResponse->childQuestionItemNode) (The child could have answer options within it or be a leaf node. It does have a question item component though).
            private Map<String, AdaptiveQuestionnaireNode> children;

            /**
             * Constructor
             * @param questionItem
             */
            public AdaptiveQuestionnaireNode(QuestionnaireItemComponent questionItem) {
                this.questionItem = questionItem;

                // The number of answer options should always equal the number of subquestion items.
                if((this.questionItem.getAnswerOption().size() != this.questionItem.getItem().size())){
                    throw new RuntimeException("There should be the same number of answer options as sub-items. Answer options: " + this.questionItem.getAnswerOption().size() + ", sub-items: " + this.questionItem.getItem().size());
                }

                Map<String, String> childIdsToResponses = new HashMap<String, String>();
                // This loop iterates over the possible answer options of this questionitem and links the linkId to its possible responses.
                for(QuestionnaireItemAnswerOptionComponent answerOption : questionItem.getAnswerOption()) {
                    // The Id of this answer response's next question.
                    String answerNextQuestionId = answerOption.getModifierExtensionFirstRep().getUrl();
                    // The response that indicates this answer to the question.
                    String possibleAnswerResponse = answerOption.getValueCoding().getCode();
                    // Check for issues.
                    if(answerNextQuestionId == null || possibleAnswerResponse == null){
                        throw new RuntimeException("Malformed Adaptive Questionnaire. Missing a questionID or answer response.");
                    }
                    // Add the key-value pair of next question id to its assocated answer response.
                    childIdsToResponses.put(answerNextQuestionId, possibleAnswerResponse);
                }

                // Create the map of answerResponses->subQuestionItems
                this.children = new HashMap<String, AdaptiveQuestionnaireNode>();
                List<QuestionnaireItemComponent> subQuestionItems = questionItem.getItem();
                for(QuestionnaireItemComponent subQuestionItem : subQuestionItems){
                    // SubQuestion linkId.
                    String subQuestionLinkId = subQuestionItem.getLinkId();
                    // SubQuestion's associated response.
                    String subQuestionResponse = childIdsToResponses.get(subQuestionLinkId);
                    // Create a new node for this subQuestion.
                    AdaptiveQuestionnaireNode subQuestionNode = new AdaptiveQuestionnaireNode(subQuestionItem);
                    this.children.put(subQuestionResponse, subQuestionNode);
                }
            }

            /**
             * Returns whether this questionniare is a leaf node.
             * @return
             */
            private boolean isLeafNode() {
                return this.children.size() < 1;
            }

            /**
             * Returns the child associated with this node for the given response.
             * @param response
             * @return
             */
            private AdaptiveQuestionnaireNode getChildForResponse(String response) {
                return this.children.get(response);
            }

            /**
             * Returns the question linkid for this node question.
             * @return
             */
            public String getQuestionId() {
                return this.questionItem.getLinkId();
            }
        }
    }

    // Logger.
    private static Logger logger = Logger.getLogger(Application.class.getName());
    // Trees that track the current and next questions. Is key-value mappng of: Map<Questionnaire ID -> AdaptiveQuestionnaireTree>
    private static final Map<String, AdaptiveQuestionnaireTree> questionnaireTrees = new HashMap<String, AdaptiveQuestionnaireTree>();

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

            String questionnaireId = inputQuestionnaireFromRequest.getId();

            if (inputQuestionnaireFromRequest != null) {

                // If there are no item answer responses in the sent JSON, reset the tree so that we can restart the question process.
                if(inputQuestionnaireFromRequest.getItem().size() < 1) {
                    questionnaireTrees.remove(questionnaireId);
                }

                if(!questionnaireTrees.containsKey(questionnaireId)){
                    // If there is not already a tree that matches the requested questionnaire id, build it.
                    // Import the requested CDS-Library Questionnaire.
                    Questionnaire cdsQuestionnaire = null;
                    try {
                        //TODO: need to determine topic, filename, and fhir version without having them hard coded
                        // File is pulled from the file store
                        FileResource fileResource = fileStore.getFile("HomeOxygenTherapy", "Questions-HomeOxygenTherapyAdditional.json", "R4", false);
                        cdsQuestionnaire = (Questionnaire) parser.parseResource(fileResource.getResource().getInputStream());
                        logger.info("--- Imported Questionnaire " + cdsQuestionnaire.getId());
                    } catch (DataFormatException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(cdsQuestionnaire == null) {
                        throw new RuntimeException("Requested CDS Questionnaire \'" + questionnaireId + "\' was not imported and may not exist.");
                    }
                    // Pull the first question from the CDS Questionnaire because it should be the top-level question (and the only item in the list).
                    QuestionnaireItemComponent topQuestionItem = cdsQuestionnaire.getItemFirstRep();
                    topQuestionItem = removeChildrenFromQuestionItem(topQuestionItem);
                    // Add the first Question item to the contained Questionnaire in the response/request QuestionnaireResponse JSON as part of the response.
                    inputQuestionnaireFromRequest.addItem(topQuestionItem);

                    // Build the tree and don't expect any answers since we only just received the required questions.
                    AdaptiveQuestionnaireTree newTree = new AdaptiveQuestionnaireTree(cdsQuestionnaire);
                    questionnaireTrees.put(questionnaireId, newTree);
                    logger.info("--- Built Questionnaire Tree for " + questionnaireId);
                } else {
                    // If there is already a tree with the requested questionnaire id, execute next-question on it with the new request.
                    // Pull the current tree for the requested questionnaire id.
                    AdaptiveQuestionnaireTree currentTree = questionnaireTrees.get(questionnaireId);
                    // Get the previous question Id.
                    String previousQuestionId = currentTree.getCurrentQuestionId();
                    // Get the request's answer component of the item with the previous question id.
                    List<QuestionnaireResponseItemComponent> allQuestions = inputQuestionnaireResponse.getItem();
                    allQuestions = allQuestions.stream().filter((QuestionnaireResponseItemComponent item) -> item.getLinkId().equals(previousQuestionId)).collect(Collectors.toList());
                    QuestionnaireResponseItemAnswerComponent answerComponent = allQuestions.get(0).getAnswerFirstRep();
                    // Pull the string response the person gave.
                    String response = answerComponent.getValueCoding().getCode();
                    // Pull the resulting next question that the recieved response points to from the tree without including its children.
                    QuestionnaireItemComponent nextQuestionItemResult = currentTree.getNextQuestionForResponse(response);
                    nextQuestionItemResult = removeChildrenFromQuestionItem(nextQuestionItemResult);
                    // Add the next question to the QuestionnaireResponse.contained[0].item[].
                    Questionnaire containedQuestionnaire = (Questionnaire) inputQuestionnaireResponse.getContained().get(0);
                    containedQuestionnaire.addItem(nextQuestionItemResult);
                    logger.info("--- Added next question for questionnaire \'" + questionnaireId + "\' for response \'" + response + "\'.");

                    // If this question is a leaf node and is the final question, set the status to "completed"
                    if (currentTree.reachedLeafNode()) {
                        inputQuestionnaireResponse.setStatus(QuestionnaireResponseStatus.COMPLETED);
                        logger.info("--- Questionnaire leaf node reached, setting status to \"completed\".");
                    }
                }

                logger.info("---- Get meta profile " + inputQuestionnaireFromRequest.getMeta().getProfile().get(0).getValue());
                
                // Build and send the response.
                String formattedResourceString = ctx.newJsonParser().encodeResourceToString(inputQuestionnaireResponse);
                return ResponseEntity.status(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_TYPE, "application/fhir+json" + "; charset=utf-8")
                        .body(formattedResourceString);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_TYPE, "application/fhir+json" + "; charset=utf-8")
                        .body("Invalid input questionnaire does not exist");
            }

        }
    }

    /**
     * Returns a new question item that is indentical to the input qusetion item except without the children.
     * @param inputQuestionItem
     * @return
     */
    private static QuestionnaireItemComponent removeChildrenFromQuestionItem(QuestionnaireItemComponent inputQuestionItem){
        QuestionnaireItemComponent questionItemNoChildren = new QuestionnaireItemComponent();
        questionItemNoChildren.setLinkId(inputQuestionItem.getLinkId());
        questionItemNoChildren.setText(inputQuestionItem.getText());
        questionItemNoChildren.setType(inputQuestionItem.getType());
        questionItemNoChildren.setRequired(inputQuestionItem.getRequired());
        questionItemNoChildren.setAnswerOption(inputQuestionItem.getAnswerOption());
        return questionItemNoChildren;
    }
}