package org.hl7.davinci.endpoint.fhir.r4;

import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.FhirBundleProcessor;
import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.files.QuestionnaireEmbeddedCQLProcessor;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: handle operation being passed one or more canonicals specifying the URL and, optionally, the version of the Questionnaire(s) to retrieve

public class QuestionnairePackageOperation {

    static final Logger logger = LoggerFactory.getLogger(QuestionnairePackageOperation.class);

    FileStore fileStore;
    String baseUrl;
    QuestionnaireEmbeddedCQLProcessor cqlProcessor;

    // map of Resources and ids/urls so that we can skip retrieving duplicates
    HashMap<String, Resource> resources = new HashMap<>();

    public QuestionnairePackageOperation(FileStore fileStore, String baseUrl) {
        this.fileStore = fileStore;
        this.baseUrl = baseUrl;
        this.cqlProcessor = new QuestionnaireEmbeddedCQLProcessor();
    }

    /*
     * Do the work retrieving all of the Questionnaire, Library and Valueset Resources.
     */
    public String execute(String resourceString, String questionnaireId) {
        Parameters outputParameters = new Parameters();
        IBaseResource resource = null;

        try {
            resource = org.hl7.davinci.r4.Utilities.parseFhirData(resourceString);
        } catch (DataFormatException exception) {
            logger.error("Failed to process input parameters: " + exception.getMessage());
            return null;
        }

        if (resource.fhirType().equalsIgnoreCase("Parameters")) {
            Parameters parameters = (Parameters) resource;

            //TODO: handle multiple FHIR Coverage Resources
            Coverage coverage = (Coverage) getResource(parameters, "coverage");

            // list of all of the orders
            Bundle orders = getAllResources(parameters, "order");
            if (coverage == null || orders.isEmpty()) {
                logger.error("Failed to find order or coverage within parameters");
                return null;
            }

            // create a single new bundle for all of the resources
            Bundle completeBundle = new Bundle();

            // list of items in bundle to avoid duplicates
            List<String> bundleContents = new ArrayList<>();
            if (questionnaireId == null) {
                // process the orders to find the topics
                FhirBundleProcessor fhirBundleProcessor = new FhirBundleProcessor(fileStore, baseUrl);
                Bundle coverageBundle = new Bundle(); // TODO - No coverages here, so an empty bundle.
                fhirBundleProcessor.processDeviceRequests(orders, coverageBundle);
                fhirBundleProcessor.processMedicationRequests(orders, coverageBundle);
                fhirBundleProcessor.processServiceRequests(orders, coverageBundle);
                fhirBundleProcessor.processMedicationDispenses(orders, coverageBundle);
                List<String> topics = createTopicList(fhirBundleProcessor);
                for (String topic : topics) {
                    logger.info("--> process topic: " + topic);
                    // get all of the Questionnaires for the topic
                    Bundle bundle = fileStore.getFhirResourcesByTopicAsFhirBundle("R4", "Questionnaire", topic.toLowerCase(), baseUrl);
                    List<BundleEntryComponent> bundleEntries = bundle.getEntry();
                    for (BundleEntryComponent entry : bundleEntries) {
                        processResource(entry.getResource(), bundleContents, completeBundle);
                    } // Questionnaires
                } // topics
            } else {
                // get only the specified Questionnaire
                Resource questionnaireResource = fileStore.getFhirResourceByIdAsFhirResource("R4", "Questionnaire", questionnaireId, baseUrl);
                if (questionnaireResource != null) {
                    processResource(questionnaireResource, bundleContents, completeBundle);
                }
            }

            // Prepopulate the QuestionnaireResponse
            if (questionnaireId != null) {
                Resource questionnaireResource = fileStore.getFhirResourceByIdAsFhirResource("R4", "Questionnaire", questionnaireId, baseUrl);
                if (questionnaireResource != null && questionnaireResource.fhirType().equalsIgnoreCase("Questionnaire")) {
                    Questionnaire questionnaire = (Questionnaire) questionnaireResource;
                    QuestionnaireResponse questionnaireResponse = prepopulateQuestionnaireResponse(questionnaire, parameters);

                    // Add mandatory extensions
                    Map<String, Object> cqlResults = new HashMap<>(); // Replace with actual CQL results
                    addMandatoryExtensions(questionnaireResponse, cqlResults);

                    // Process answers
                    processAnswers(questionnaireResponse, completeBundle);

                    ParametersParameterComponent responseParameter = new ParametersParameterComponent();
                    responseParameter.setName("questionnaire-response");
                    responseParameter.setResource(questionnaireResponse);
                    outputParameters.addParameter(responseParameter);
                }
            }

            // add the bundle to the output parameters if it contains any resources
            if (!completeBundle.isEmpty()) {
                ParametersParameterComponent parameter = new ParametersParameterComponent();
                parameter.setName("return");
                parameter.setResource(completeBundle);
                outputParameters.addParameter(parameter);
            } else {
                logger.info("No matching Questionnaires found");
            }
        }

        // if none found return null
        if (outputParameters.isEmpty()) {
            return null;
        }

        // convert the outputParameters to a string
        FhirContext ctx = new org.hl7.davinci.r4.FhirComponents().getFhirContext();
        IParser parser = ctx.newJsonParser().setPrettyPrint(true);
        return parser.encodeResourceToString(outputParameters);
    }

    private Resource getResource(Parameters parameters, String name) {
        for (ParametersParameterComponent parameter : parameters.getParameter()) {
            if (parameter.getName().equals(name))
                return parameter.getResource();
        }
        return null;
    }

    private Bundle getAllResources(Parameters parameters, String name) {
        Bundle foundResources = new Bundle();
        for (ParametersParameterComponent parameter : parameters.getParameter()) {
            if (parameter.getName().equals(name))
                foundResources.addEntry(new BundleEntryComponent().setResource(parameter.getResource()));
        }

        return foundResources;
    }

    private boolean addResourceToBundle(Resource resource, List<String> bundleContents, Bundle questionnaireBundle) {
        // only add the library if not already in the bundle
        boolean added = false;
        if (!bundleContents.contains(resource.getId())) {
            // add the questionnaire to the bundle
            BundleEntryComponent questionnaireBundleEntry = new BundleEntryComponent();
            questionnaireBundleEntry.setResource(resource);
            questionnaireBundle.addEntry(questionnaireBundleEntry);
            bundleContents.add(resource.getId());
            logger.info("    --> add " + resource.fhirType() + ": " + resource.getId());
            added = true;
        }
        return added;
    }

    private List<String> createTopicList(FhirBundleProcessor fhirBundleProcessor) {
        List<String> topics = new ArrayList<>();
        List<CoverageRequirementRuleResult> results = fhirBundleProcessor.getResults();
        for (CoverageRequirementRuleResult result : results) {
            // add topic to the list if not already contained
            if (!topics.contains(result.getTopic())) {
                topics.add(result.getTopic());
            }
        }
        return topics;
    }

    private void processResource(Resource resource, List<String> bundleContents, Bundle completeBundle) {
        addResourceToBundle(resource, bundleContents, completeBundle);

        if (resource.fhirType().equalsIgnoreCase("Questionnaire")) {
            Questionnaire questionnaire = (Questionnaire) resource;

            List<Extension> extensions = questionnaire.getExtension();
            for (Extension extension : extensions) {
                if (extension.getUrl().endsWith("cqf-library")) {
                    CanonicalType data = (CanonicalType) extension.getValue();
                    String url = data.asStringValue();
                    Resource libraryResource = null;

                    // look in the map and retrieve it instead of looking it up on disk if found
                    if (resources.containsKey(url)) {
                        libraryResource = resources.get(url);
                    } else {
                        libraryResource = fileStore.getFhirResourceByUrlAsFhirResource("R4", "Library", url, baseUrl);
                        resources.put(url, libraryResource);
                    }

                    if (addResourceToBundle(libraryResource, bundleContents, completeBundle)) {
                        // recursively add the depends-on libraries if added to bundle
                        addLibraryDependencies((Library) libraryResource, bundleContents, completeBundle);
                    }
                }
            }
        }
    }

    /*
     * Recursively add all of the libraries dependencies related by the "depends-on" type.
     */
    private void addLibraryDependencies(Library library, List<String> bundleContents, Bundle questionnaireBundle) {
        List<RelatedArtifact> relatedArtifacts = library.getRelatedArtifact();
        for (RelatedArtifact relatedArtifact : relatedArtifacts) {
            // only add the depends-on artifacts
            if (relatedArtifact.getType() == RelatedArtifact.RelatedArtifactType.DEPENDSON) {
                String relatedArtifactReferenceString = relatedArtifact.getResource();
                String[] referenceParts = relatedArtifactReferenceString.split("/");
                String id = referenceParts[1];
                Resource referencedLibraryResource = null;

                // look in the map and retrieve it instead of looking it up on disk if found
                if (resources.containsKey(id)) {
                    referencedLibraryResource = resources.get(id);
                } else {
                    referencedLibraryResource = fileStore.getFhirResourceByIdAsFhirResource("R4", "Library", id, baseUrl);
                    resources.put(id, referencedLibraryResource);
                }

                // only add the library if not already in the bundle
                if (!bundleContents.contains(referencedLibraryResource.getId())) {
                    BundleEntryComponent referencedLibraryBundleEntry = new BundleEntryComponent();
                    referencedLibraryBundleEntry.setResource(referencedLibraryResource);
                    questionnaireBundle.addEntry(referencedLibraryBundleEntry);
                    bundleContents.add(referencedLibraryResource.getId());

                    // recurse through the libraries...
                    addLibraryDependencies((Library) referencedLibraryResource, bundleContents, questionnaireBundle);
                }
            }
        }

        // grab all of the ValueSets in the DataRequirement
        List<DataRequirement> dataRequirements = library.getDataRequirement();
        for (DataRequirement dataRequirement : dataRequirements) {
            List<DataRequirement.DataRequirementCodeFilterComponent> codeFilters = dataRequirement.getCodeFilter();
            for (DataRequirement.DataRequirementCodeFilterComponent codeFilter : codeFilters) {
                String valueSetUrl = codeFilter.getValueSet();
                Resource valueSetResource = null;

                // look in the map and retrieve it instead of looking it up on disk if found
                if (resources.containsKey(valueSetUrl)) {
                    valueSetResource = resources.get(valueSetUrl);
                } else {
                    valueSetResource = fileStore.getFhirResourceByUrlAsFhirResource("R4", "ValueSet", valueSetUrl, baseUrl);
                    resources.put(valueSetUrl, valueSetResource);
                }

                addResourceToBundle(valueSetResource, bundleContents, questionnaireBundle);
            }
        }
    }

    private QuestionnaireResponse prepopulateQuestionnaireResponse(Questionnaire questionnaire, Parameters parameters) {
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        questionnaireResponse.setQuestionnaire(questionnaire.getIdElement().toString());

        for (Questionnaire.QuestionnaireItemComponent item : questionnaire.getItem()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent responseItem = prepopulateItem(item, parameters);
            questionnaireResponse.addItem(responseItem);
        }

        return questionnaireResponse;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent prepopulateItem(Questionnaire.QuestionnaireItemComponent item, Parameters parameters) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent responseItem = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        responseItem.setLinkId(item.getLinkId());
        responseItem.setText(item.getText());

        // Retrieve CQL expression for the item
        String cqlExpression = getCqlExpressionForItem(item);

        // Execute CQL expression if available
        if (cqlExpression != null) {
            Object result = executeCqlExpression(cqlExpression);
            if (result != null) {
                QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
                answer.setValue(new StringType(result.toString()));
                responseItem.addAnswer(answer);
            }
        } else {
            // If no CQL expression, create empty answers based on answer options
            for (Questionnaire.QuestionnaireItemAnswerOptionComponent option : item.getAnswerOption()) {
                QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
                answer.setValue(option.getValue());
                responseItem.addAnswer(answer);
            }
        }

        // Prepopulate child items recursively
        for (Questionnaire.QuestionnaireItemComponent childItem : item.getItem()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent responseChildItem = prepopulateItem(childItem, parameters);
            responseItem.addItem(responseChildItem);
        }

        return responseItem;
    }

    private String getCqlExpressionForItem(Questionnaire.QuestionnaireItemComponent item) {
        // This assumes there is a custom extension or element in the item that holds the CQL expression
        Extension cqlExtension = item.getExtensionByUrl("http://example.com/fhir/StructureDefinition/cql-expression");
        if (cqlExtension != null && cqlExtension.getValue() instanceof StringType) {
            return ((StringType) cqlExtension.getValue()).getValue();
        }
        return null;
    }

    private Object executeCqlExpression(String cqlExpression) {
        String elm = null;
        try {
            // Translate CQL expression to Elm or execute directly
            elm = CqlExecution.translateToElm(cqlExpression, this.cqlProcessor);
        } catch (Exception e) {
            logger.error("Failed to Execute Cql Expression");
        }
        return elm != null ? elm : null;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent prepopulateItem(Questionnaire.QuestionnaireItemComponent item, Parameters parameters, Map<String, Object> cqlResults) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent responseItem = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        responseItem.setLinkId(item.getLinkId());
        responseItem.setText(item.getText());

        // Check if CQL results contain data for this item
        if (cqlResults.containsKey(item.getLinkId())) {
            Object cqlResult = cqlResults.get(item.getLinkId());

            // Use the CQL result to prepopulate the answer
            QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
            answer.setValue(new StringType(cqlResult.toString()));
            responseItem.addAnswer(answer);
        } else {
            // If no CQL result, add empty answers based on answer options
            for (Questionnaire.QuestionnaireItemAnswerOptionComponent option : item.getAnswerOption()) {
                QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
                answer.setValue(option.getValue());
                responseItem.addAnswer(answer);
            }
        }

        // Prepopulate child items recursively
        for (Questionnaire.QuestionnaireItemComponent childItem : item.getItem()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent responseChildItem = prepopulateItem(childItem, parameters, cqlResults);
            responseItem.addItem(responseChildItem);
        }

        return responseItem;
    }


    private void addMandatoryExtensions(QuestionnaireResponse response, Map<String, Object> cqlResults) {
        for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : response.getItem()) {
            addExtensionsToItem(item, cqlResults);
            addIntendedUseExtension(item, cqlResults);
            addContextExtension(item, cqlResults);
        }
    }


    private void addExtensionsToItem(QuestionnaireResponse.QuestionnaireResponseItemComponent item, Map<String, Object> cqlResults) {
        // Adding an extension for CQL result if applicable
        if (cqlResults.containsKey(item.getLinkId())) {
            Object cqlResult = cqlResults.get(item.getLinkId());
            Extension extension = new Extension();
            extension.setUrl("http://example.com/fhir/StructureDefinition/cql-result");
            extension.setValue(new StringType(cqlResult.toString()));
            item.addExtension(extension);
        }

        // Recursively add extensions to child items
        for (QuestionnaireResponse.QuestionnaireResponseItemComponent childItem : item.getItem()) {
            addExtensionsToItem(childItem, cqlResults);
        }
    }

    private void addIntendedUseExtension(QuestionnaireResponse.QuestionnaireResponseItemComponent item, Map<String, Object> cqlResults) {
        // Adding intendedUse extension if available in cqlResults
        if (cqlResults.containsKey("intendedUse")) {
            Object intendedUseValue = cqlResults.get("intendedUse");

            Extension intendedUseExtension = new Extension();
            intendedUseExtension.setUrl("http://hl7.org/fhir/us/davinci-dtr/StructureDefinition/intendedUse");

            if (intendedUseValue instanceof String) {
                intendedUseExtension.setValue(new StringType((String) intendedUseValue));
            } else if (intendedUseValue instanceof CodeType) {
                intendedUseExtension.setValue((CodeType) intendedUseValue);
            } else {
                // Handle other types as needed
                logger.warn("Unexpected type for intendedUseValue: " + intendedUseValue.getClass().getName());
                return;
            }

            item.addExtension(intendedUseExtension);
        }
    }

    private void addContextExtension(QuestionnaireResponse.QuestionnaireResponseItemComponent item, Map<String, Object> cqlResults) {
        // Adding context extension if available in cqlResults
        if (cqlResults.containsKey("context")) {
            Object contextValue = cqlResults.get("context");

            Extension contextExtension = new Extension();
            contextExtension.setUrl("http://hl7.org/fhir/us/davinci-dtr/StructureDefinition/qr-context");

            if (contextValue instanceof String) {
                contextExtension.setValue(new StringType((String) contextValue));
            } else {
                // Handle other types as needed
                logger.warn("Unexpected type for contextValue: " + contextValue.getClass().getName());
                return;
            }

            item.addExtension(contextExtension);
        }
    }
    
    private void processAnswers(QuestionnaireResponse questionnaireResponse, Bundle bundle) {
        for (QuestionnaireResponse.QuestionnaireResponseItemComponent item : questionnaireResponse.getItem()) {
            processItemAnswers(item);
        }
    }

    private void processItemAnswers(QuestionnaireResponse.QuestionnaireResponseItemComponent item) {
        for (QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer : item.getAnswer()) {
            if (answer.hasValueCoding()) {
                Coding coding = answer.getValueCoding();

                if ("http://hl7.org/fhir/us/davinci-dtr/CodeSystem/temp".equals(coding.getSystem())) {
                    switch (coding.getCode()) {
                        case "auto":
                            addInformationOriginExtension(answer, "Auto populated");
                            break;
                        case "manual":
                            addInformationOriginExtension(answer, "Manual entry");
                            break;
                        case "override":
                            addInformationOriginExtension(answer, "Auto populated but overridden by a human");
                            break;
                    }
                }
            }
        }
        for (QuestionnaireResponse.QuestionnaireResponseItemComponent subItem : item.getItem()) {
            processItemAnswers(subItem);
        }
    }

    private void addInformationOriginExtension(QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer, String display) {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/us/davinci-dtr/CodeSystem/temp");
        coding.setCode(displayToCode(display));
        coding.setDisplay(display);

        Extension informationOriginExtension = new Extension("http://hl7.org/fhir/us/davinci-dtr/StructureDefinition/informationOrigin");
        informationOriginExtension.setValue(coding);

        answer.addExtension(informationOriginExtension);
    }

    private String displayToCode(String display) {
        switch (display) {
            case "Auto populated":
                return "auto";
            case "Manual entry":
                return "manual";
            case "Auto populated but overridden by a human":
                return "override";
            default:
                return null;
        }
    }

}
