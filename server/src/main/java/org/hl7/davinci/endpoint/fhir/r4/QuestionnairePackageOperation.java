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
                    // get all of the Quesionnaires for the topic
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

            // Use the embedded CQL processor to process the questionnaire
            questionnaire = cqlProcessor.replaceEmbeddedCql(questionnaire);

            // Prepopulate the QuestionnaireResponse using the processed Questionnaire
            QuestionnaireResponse questionnaireResponse = prepopulateQuestionnaireResponse(questionnaire);

            // Add prepopulated QuestionnaireResponse to the complete bundle
            addResourceToBundle(questionnaireResponse, bundleContents, completeBundle);
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

    private QuestionnaireResponse prepopulateQuestionnaireResponse(Questionnaire questionnaire) {
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();

        // Set the basic information for the QuestionnaireResponse
        questionnaireResponse.setQuestionnaire(questionnaire.getUrl());
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS);
        questionnaireResponse.setSubject(new Reference("Patient/123")); // Example patient reference, replace as needed

        // Use CqlExecution to evaluate the CQL expressions and populate the questionnaireResponse
        Map<String, Object> cqlResults = executeCqlForQuestionnaire(questionnaire);

        for (Questionnaire.QuestionnaireItemComponent item : questionnaire.getItem()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent responseItem = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
            responseItem.setLinkId(item.getLinkId());
            responseItem.setText(item.getText());

            // Assume each item has an associated CQL expression
            String cqlExpression = getCqlExpressionForItem(item);

            if (cqlExpression != null && cqlResults.containsKey(cqlExpression)) {
                Object result = cqlResults.get(cqlExpression);

                // Populate the response item based on the CQL result
                if (result instanceof Boolean) {
                    responseItem.addAnswer(new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().setValue(new BooleanType((Boolean) result)));
                } else if (result instanceof String) {
                    responseItem.addAnswer(new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().setValue(new StringType((String) result)));
                } else if (result instanceof Integer) {
                    responseItem.addAnswer(new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().setValue(new IntegerType((Integer) result)));
                } else if (result instanceof Double) {
                    responseItem.addAnswer(new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent().setValue(new DecimalType((Double) result)));
                }
            }

            questionnaireResponse.addItem(responseItem);
        }

        // Add mandatory extensions: context, intendedUse, and informationOrigin
        addMandatoryExtensions(questionnaireResponse, cqlResults);

        return questionnaireResponse;
    }

    private Map<String, Object> executeCqlForQuestionnaire(Questionnaire questionnaire) {
        Map<String, Object> results = new HashMap<>();
        // for each item in the questionnaire, execute the associated CQL expression
        for (Questionnaire.QuestionnaireItemComponent item : questionnaire.getItem()) {
            String cqlExpression = getCqlExpressionForItem(item);
            if (cqlExpression != null) {
                Object result = executeCqlExpression(cqlExpression);
                results.put(cqlExpression, result);
            }
        }
        return results;
    }

    private Object executeCqlExpression(String cqlExpression) {
        String elm = null;
        try {
            elm = CqlExecution.translateToElm(cqlExpression, this.cqlProcessor);
        } catch (Exception e) {
            logger.error("Failed to Execute Cql Expression");
        }
        return elm != null ? elm : null;
    }

    private String getCqlExpressionForItem(Questionnaire.QuestionnaireItemComponent item) {
        // This assumes there is a custom extension or element in the item that holds the CQL expression
        Extension cqlExtension = item.getExtensionByUrl("http://example.com/fhir/StructureDefinition/cql-expression");
        if (cqlExtension != null && cqlExtension.getValue() instanceof StringType) {
            return ((StringType) cqlExtension.getValue()).getValue();
        }
        return null;
    }

    private void addMandatoryExtensions(QuestionnaireResponse questionnaireResponse, Map<String, Object> cqlResults) {
        // Create and set the context extension
        Extension contextExtension = new Extension("http://hl7.org/fhir/us/davinci-dtr/StructureDefinition/qr-context");
        if (cqlResults.containsKey("context")) {
            contextExtension.setValue(new StringType((String) cqlResults.get("context")));
        }
        questionnaireResponse.addExtension(contextExtension);

        // Create and set the intended use extension
        Extension intendedUseExtension = new Extension("http://hl7.org/fhir/us/davinci-dtr/StructureDefinition/intendedUse");
        if (cqlResults.containsKey("intendedUse")) {
            intendedUseExtension.setValue(new StringType((String) cqlResults.get("intendedUse")));
        }
        questionnaireResponse.addExtension(intendedUseExtension);

        // Create and set the intended use extension
        Extension informationOrigin = new Extension("http://hl7.org/fhir/us/davinci-dtr/StructureDefinition/information-origin");
        if (cqlResults.containsKey("origin")) {
            informationOrigin.setValue(new StringType((String) cqlResults.get("origin")));
        }
        questionnaireResponse.addExtension(informationOrigin);
    }
}
