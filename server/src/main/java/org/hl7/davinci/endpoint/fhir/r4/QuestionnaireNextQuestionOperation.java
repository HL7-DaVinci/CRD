package org.hl7.davinci.endpoint.fhir.r4;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.hl7.davinci.endpoint.files.FileResource;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.files.QuestionnaireEmbeddedCQLProcessor;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemAnswerOptionComponent;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent;
import org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseItemComponent;
import org.hl7.fhir.r4.model.QuestionnaireResponse.QuestionnaireResponseStatus;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;


// --- ORDER OF RESPONSE-REQUEST OPERATIONS
// (REQUEST) External user sends the initial QuestionnaireResponse JSON that contains which questionnaire it would like to trigger as n element the "contained" field.
// (RESPONSE) QuestionnaireController adds the first question with its answerResponse options (with its linkId and text) to the JSON in QuestionnaireResponse.contained.item[] and sends it back.
// (REQUEST) External user adds their answer to the question to the JSON in QuestionnaireResponse.item[] and sends it back.
// (RESPONSE) QuestionnaireController takes that response and adds the next indicated question to the JSON in QuestionnaireResponse.contained.item[] and sends it back.
// Repeat intil QuestionnaireController reaches a leaf-node, then it sets the status to "completed" from "in-progress"
// Ultimately, The QuestionnaireController responses add ONLY to the QuestionnaireResponse.contained.item[]. The external requester adds answers to QuestionnaireResponse.item[] and includes the associated linkid and text.

public class QuestionnaireNextQuestionOperation {
    
    FileStore fileStore;

    private QuestionnaireEmbeddedCQLProcessor questionnaireEmbeddedCQLProcessor;

     // Logger.
     private static Logger logger = Logger.getLogger(QuestionnaireNextQuestionOperation.class.getName());
     // Trees that track the current and next questions. Is key-value mappng of: Map<Questionnaire ID -> AdaptiveQuestionnaireTree>
     private static final Map<String, AdaptiveQuestionnaireTree> questionnaireTrees = new HashMap<String, AdaptiveQuestionnaireTree>();

    public QuestionnaireNextQuestionOperation(FileStore fileStore) {
        this.fileStore = fileStore;
        this.questionnaireEmbeddedCQLProcessor = new QuestionnaireEmbeddedCQLProcessor();
    }

    /**
     * An inner class that demos a tree to define next questions based on responses.
     */
    private class AdaptiveQuestionnaireTree {
        
        // The initial question node of the tree.
        private AdaptiveQuestionnaireNode root;
    
        /**
         * Initial constructor that generates the beginning of the tree.
         * @param inputQuestionnaire    The input questionnaire from the CDS-Library.
         */
        public AdaptiveQuestionnaireTree(Questionnaire inputQuestionnaire) {
            // Top level parent question item; the first set of questions.
            QuestionnaireItemComponent topLevelQuestion = inputQuestionnaire.getItemFirstRep();
            // Start the root building.
            this.root = new AdaptiveQuestionnaireNode(topLevelQuestion);
        }

        /**
         * Returns the next question based on the response to the current question. Also sets the next question based on that response.
         * @param inputQuestionnaireResponse
         * @param allAnswerItems  The set of answer items given to this tree.
         * @return
         */
        public List<QuestionnaireItemComponent> getNextQuestionsForAnswers(List<QuestionnaireResponseItemComponent> allResponseItems, QuestionnaireResponse inputQuestionnaireResponse) {
            if(allResponseItems == null) {
                throw new NullPointerException("Input answer items is null.");
            } else if ((new HashSet(allResponseItems.stream().map(item -> item.getLinkId()).collect(Collectors.toList()))).size() != allResponseItems.size()){
                throw new RuntimeException("Detected duplicate answers to the same question.");
            }
            return this.root.getNextQuestionForAnswers(allResponseItems, inputQuestionnaireResponse);
        }
    
        /**
         * Inner class that describes a node of the tree.
         */
        private class AdaptiveQuestionnaireNode {

            // Contains the list of additional questions that should be displayed with this question.
            private List<QuestionnaireItemComponent> supplementalQuestions;
            // Contains the current question item that dictates the next question of the node.
            private QuestionnaireItemComponent determinantQuestionItem;
            // Map of (answerResponse->childQuestionItemNode) (The child could have answer options within it or be a leaf node. It does have a question item component though).
            private Map<String, AdaptiveQuestionnaireNode> children;

            /**
             * Constructor
             * @param determinantQuestionItem
             */
            public AdaptiveQuestionnaireNode(QuestionnaireItemComponent determinantQuestion) {

                this.determinantQuestionItem = determinantQuestion;
                // Get the child and supplemental question items of this question.
                List<QuestionnaireItemComponent> subQuestions = determinantQuestion.getItem();
                // Extract the supplemental questions which do not have a child link-id branch from the determinant questions.
                List<String> nonSupplementLinkIds = determinantQuestionItem.getAnswerOption().stream().map(answerOption -> answerOption.getModifierExtensionFirstRep().getUrl()).collect(Collectors.toList());
                List<QuestionnaireItemComponent> childQuestions = this.extractChildQuestions(subQuestions, nonSupplementLinkIds);
                // Extract the remaining questions as supplemental questions.
                this.supplementalQuestions = this.extractSupplementalQuestions(subQuestions, nonSupplementLinkIds);

                // The number of answer options of the determinant question should always equal the number of child question items.
                if((this.determinantQuestionItem.getAnswerOption().size() != childQuestions.size())){
                    throw new RuntimeException("There should be the same number of answer options as sub-items. Answer options: " + this.determinantQuestionItem.getAnswerOption().size() + ", sub-items: " + childQuestions.size());
                }

                // If the determinant question item does not have any answer options, then this is a leaf node and should not generate any children.
                if(determinantQuestionItem.hasAnswerOption()) {
                    Map<String, String> childIdsToResponses = new HashMap<String, String>();
                    // This loop iterates over the possible answer options of this questionitem and links the linkId to its possible responses.
                    for(QuestionnaireItemAnswerOptionComponent answerOption : determinantQuestionItem.getAnswerOption()) {
                        // The Id of this answer response's next question.
                        String answerNextQuestionId = answerOption.getModifierExtensionFirstRep().getUrl();
                        // The response that indicates this answer to the question.
                        String possibleAnswerResponse = answerOption.getValueCoding().getCode();
                        // Check for issues.
                        if(answerNextQuestionId == null || possibleAnswerResponse == null){
                            throw new RuntimeException("Malformed Adaptive Questionnaire. Missing a question ID or answer response.");
                        }
                        // Add the key-value pair of next question id to its assocated answer response.
                        childIdsToResponses.put(answerNextQuestionId, possibleAnswerResponse);
                    }

                    // Create the map of answerResponses->subQuestionItems
                    this.children = new HashMap<String, AdaptiveQuestionnaireNode>();
                    List<QuestionnaireItemComponent> subQuestionItems = determinantQuestionItem.getItem();
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
            }

            /**
             * Returns the next question based on the set of provided answers.
             * @param allResponseItems
             * @param inputQuestionnaireResponse
             * @return
             */
            public List<QuestionnaireItemComponent> getNextQuestionForAnswers(List<QuestionnaireResponseItemComponent> allResponseItems, QuestionnaireResponse inputQuestionnaireResponse) {

                // Extract the current question being answered from the list if answer items.
                String currentQuestionId = this.determinantQuestionItem.getLinkId();
                List<QuestionnaireResponseItemComponent> currentQuestionResponses = allResponseItems.stream().filter(answerItem -> answerItem.getLinkId().equals(currentQuestionId)).collect(Collectors.toList());
                if(currentQuestionResponses.size() != 1) {
                    // If there are no more answer items to check, we've reached the end of the recursion.
                    // TODO - this could cause an unexpected end-of-questionnaire issue if incorrect responses are given.
                    return this.getQuestionSet();
                }

                QuestionnaireResponseItemComponent currentQuestionResponse = currentQuestionResponses.get(0);
                QuestionnaireResponseItemAnswerComponent currentQuestionAnswer = currentQuestionResponse.getAnswerFirstRep();

                // With the currrent question answer in hand, extract the next question.
                String response;
                if (currentQuestionAnswer.hasValueStringType()) {
                    response = currentQuestionAnswer.getValueStringType().asStringValue();
                } else if (currentQuestionAnswer.hasValueCoding()) {
                    response = currentQuestionAnswer.getValueCoding().getCode();
                } else {
                    throw new RuntimeException("Answer does not match one of the possible input types.");
                }
                if(!children.containsKey(response)){
                    throw new NullPointerException("Response does not match with a possible next question.");
                }
                AdaptiveQuestionnaireNode nextNode = this.children.get(response);

                if(nextNode.isLeafNode()){
                    // Since the next node is a leaf node, set the questionnaire response status to complete.
                    inputQuestionnaireResponse.setStatus(QuestionnaireResponseStatus.COMPLETED);
                    return nextNode.getQuestionSet();
                }
                
                // Has to be done this way without removing the previous answer response so that we don't alter the original list object.
                List<QuestionnaireResponseItemComponent> nextResponseItems = allResponseItems.stream().filter(responseItem -> !responseItem.equals(currentQuestionResponse)).collect(Collectors.toList());
                return nextNode.getNextQuestionForAnswers(nextResponseItems, inputQuestionnaireResponse);
            }

            /**
             * Returns the question items in the given list that do not have the linkids of the given list of strings.
             * @param questionItems
             * @param nonSupplementQuestions
             * @return
             */
            private List<QuestionnaireItemComponent> extractSupplementalQuestions(
                    List<QuestionnaireItemComponent> questionItems, List<String> nonSupplementLinkIds) {
                return questionItems.stream().filter(questionItem -> !nonSupplementLinkIds.contains(questionItem.getLinkId())).collect(Collectors.toList());
            }

            /**
             * Returns the question items in the given list that do have the linkids of the given list of strings.
             * @param questionItems
             * @param nonSupplementQuestions
             * @return
             */
            private List<QuestionnaireItemComponent> extractChildQuestions(
                    List<QuestionnaireItemComponent> questionItems, List<String> nonSupplementLinkIds) {
                return questionItems.stream().filter(questionItem -> nonSupplementLinkIds.contains(questionItem.getLinkId())).collect(Collectors.toList());
            }

            /**
             * Returns the set of questions associated with the node. Incldues all questions in the set, determinant and non-determinant.
             * @return
             */
            public List<QuestionnaireItemComponent> getQuestionSet() {
                QuestionnaireItemComponent determinantQuestionNoChildren = this.removeChildrenFromQuestionItem(this.determinantQuestionItem);
                List<QuestionnaireItemComponent> questionSet = new ArrayList<QuestionnaireItemComponent>();
                questionSet.add(determinantQuestionNoChildren);
                questionSet.addAll(this.supplementalQuestions);
                logger.info("--- Question Set: " + questionSet.stream().map(item -> item.getLinkId()).collect(Collectors.toList()));
                return questionSet;
            }

            /**
             * Returns a new question item that is indentical to the input qusetion item except without the children.
             * @param inputQuestionItem
             * @return
             */
            private QuestionnaireItemComponent removeChildrenFromQuestionItem(QuestionnaireItemComponent inputQuestionItem){
                QuestionnaireItemComponent questionItemNoChildren = new QuestionnaireItemComponent();
                questionItemNoChildren.setLinkId(inputQuestionItem.getLinkId());
                questionItemNoChildren.setText(inputQuestionItem.getText());
                questionItemNoChildren.setType(inputQuestionItem.getType());
                questionItemNoChildren.setRequired(inputQuestionItem.getRequired());
                questionItemNoChildren.setAnswerOption(inputQuestionItem.getAnswerOption());
                return questionItemNoChildren;
            }

            /**
             * Returns whether this questionniare is a leaf node.
             * @return
             */
            private boolean isLeafNode() {
                return this.children == null || this.children.size() < 1;
            }
        }
    }

    
     /**
     * Returns the next question based on the request.
     * @param body
     * @param request
     * @return
     */
    public ResponseEntity<String> execute(String body, HttpServletRequest request) {
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

            logger.info("--- Received questionnaire response: " + ctx.newJsonParser().encodeResourceToString(inputQuestionnaireResponse));
            // Check that there are no duplicates in the recieved set of questions.
            if ((new HashSet(((Questionnaire) inputQuestionnaireResponse.getContained().get(0)).getItem().stream().map(item -> item.getLinkId()).collect(Collectors.toList()))).size() != ((Questionnaire) inputQuestionnaireResponse.getContained().get(0)).getItem().size()){
                throw new RuntimeException("Received a set of questions with duplicates.");
            }

            String questionnaireId = ((Reference) inputQuestionnaireResponse.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/contained-id").getValue()).getReference();
            System.out.println("Input Questionnaire: " + questionnaireId);

            if (inputQuestionnaireFromRequest != null) {

                if (!questionnaireTrees.containsKey(questionnaireId)) {
                    // If there is not already a tree that matches the requested questionnaire id, build it.
                    // Import the requested CDS-Library Questionnaire.
                    Questionnaire cdsQuestionnaire = importCdsAdaptiveQuestionnaire(request, parser, fileStore, questionnaireId);
                    
                    // Build the tree.
                    AdaptiveQuestionnaireTree newTree = new AdaptiveQuestionnaireTree(cdsQuestionnaire);
                    questionnaireTrees.put(questionnaireId, newTree);
                    logger.info("--- Built Questionnaire Tree for " + questionnaireId);
                }

                // Pull the tree for the requested questionnaire id.
                AdaptiveQuestionnaireTree currentTree = questionnaireTrees.get(questionnaireId);
                // Get the request's set of answer responses.
                List<QuestionnaireResponseItemComponent> allResponses = inputQuestionnaireResponse.getItem();
                // Pull the resulting next question that the recieved responses and answers point to from the tree without including its children.
                List<QuestionnaireItemComponent> nextQuestionSetResults = currentTree.getNextQuestionsForAnswers(allResponses, inputQuestionnaireResponse);
                // Add the next set of questions to the response.
                QuestionnaireNextQuestionOperation.addQuestionSetToQuestionnaireResponse(inputQuestionnaireResponse, nextQuestionSetResults);
                // Check that there no duplicates in the set of questions.
                if ((new HashSet(((Questionnaire) inputQuestionnaireResponse.getContained().get(0)).getItem().stream().map(item -> item.getLinkId()).collect(Collectors.toList()))).size() != ((Questionnaire) inputQuestionnaireResponse.getContained().get(0)).getItem().size()){
                    throw new RuntimeException("Attempted to send a set of questions with duplicates. Question IDs are: " + (((Questionnaire) inputQuestionnaireResponse.getContained().get(0)).getItem().stream().map(item -> item.getLinkId()).collect(Collectors.toList())));
                }

                logger.info("--- Added next question set for questionnaire \'" + questionnaireId + "\' for responses \'" + allResponses + "\'.");

                // Build and send the response.
                String formattedResourceString = ctx.newJsonParser().encodeResourceToString(inputQuestionnaireResponse);
                logger.info("--- Sending questionnaire response: " + formattedResourceString);
                return ResponseEntity.status(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_TYPE, "application/fhir+json" + "; charset=utf-8")
                        .body(formattedResourceString);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.CONTENT_TYPE, "application/fhir+json" + "; charset=utf-8")
                        .body("Input questionnaire from the request does not exist.");
            }
        }
    }

    /**
     * Imports the requested questionnaire from the CDS-Library.
     * @param fileStore2
     * @param parser
     * @param request
     * @return
     */
    private Questionnaire importCdsAdaptiveQuestionnaire(HttpServletRequest request, IParser parser, FileStore fileStore, String questionnaireId) {
        Questionnaire cdsQuestionnaire = null;
        try {
            String adaptiveQuestionniareFile = ("Questions-" + questionnaireId + "Adaptive.json").replace("#", ""); // The filename should be the questionnaire ID with these added values.
            String topic = questionnaireId.replace("Additional", "").replace("#", ""); // The topic should be the questionnaire ID but without the 'Additional' tag.
            // File is pulled from the file store as a file.
            logger.info("--- Importing questionniare file: " + adaptiveQuestionniareFile + " from topic: " + topic);
            FileResource fileResource = fileStore.getFile(topic, adaptiveQuestionniareFile, "R4", false);
            if(fileResource == null) {
                throw new RuntimeException("File resource pulled from the filestore is null.");
            }
            if(fileResource.getResource() == null) {
                throw new RuntimeException("File resource pulled from the filestore has a null getResource().");
            }
            cdsQuestionnaire = (Questionnaire) parser.parseResource(fileResource.getResource().getInputStream());
            cdsQuestionnaire = this.questionnaireEmbeddedCQLProcessor.processResource(cdsQuestionnaire, null, null);
            logger.info("--- Imported Questionnaire " + cdsQuestionnaire.getId());
        } catch (DataFormatException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cdsQuestionnaire;
    }

    /**
     * Adds the given set of questions to the contained questionniare in the questionnaire response.
     * @param inputQuestionnaireResponse
     * @param questionSet
     */
    private static void addQuestionSetToQuestionnaireResponse(QuestionnaireResponse inputQuestionnaireResponse, List<QuestionnaireItemComponent> questionSet) {
        // Add the next question set to the QuestionnaireResponse.contained[0].item[].
        Questionnaire containedQuestionnaire = (Questionnaire) inputQuestionnaireResponse.getContained().get(0);
        questionSet.forEach(questionItem -> containedQuestionnaire.addItem(questionItem));
    }
}
