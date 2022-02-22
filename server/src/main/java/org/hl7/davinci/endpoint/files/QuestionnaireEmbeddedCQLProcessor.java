package org.hl7.davinci.endpoint.files;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.hl7.davinci.endpoint.cql.CqlExecution;
import org.hl7.fhir.r4.model.Questionnaire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hl7.elm.r1.VersionedIdentifier;
import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r4.model.Expression;
import org.hl7.fhir.r4.model.Extension;

public class QuestionnaireEmbeddedCQLProcessor extends FhirResourceProcessor<Questionnaire>
        implements LibrarySourceProvider {

    static final Logger logger = LoggerFactory.getLogger(QuestionnaireEmbeddedCQLProcessor.class);

    @Override
    protected Questionnaire processResource(Questionnaire inputResource, FileStore fileStore, String baseUrl) {
        // TODO Auto-generated method stub
        return this.replaceEmbeddedCql(inputResource);
    }

    protected Questionnaire replaceEmbeddedCql(Questionnaire inputResource) {
        // find expressions and language as text/cql
        List<QuestionnaireItemComponent> itemList = inputResource.getItem();
        findAndReplaceEmbeddedCql(itemList);
        return inputResource;
    }

    private void findAndReplaceEmbeddedCql(List<QuestionnaireItemComponent> itemComponents) {
        for (QuestionnaireItemComponent itemComponent : itemComponents) {
            if (hasEmbeddedCql(itemComponent)) {
                List<Extension> extensionList = itemComponent.getExtension();

                for (int i = 0; i < extensionList.size(); i++) {
                    Extension extension = extensionList.get(i);
                    if (extension.getUrl()
                            .equals("http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-initialExpression")) {
                        Expression expression = (Expression) extension.getValue();
                        if (expression.getLanguage().equals("text/cql")) {
                            String expressionString = expression.getExpression();
                            // regex for \"library\".statement
                            final String libraryRefRegex = "^\\\\\\\"[a-zA-Z0-9]+\\\\\\\".[a-zA-Z0-9]+$";
                            final Pattern pattern = Pattern.compile(libraryRefRegex, Pattern.MULTILINE);
                            // if not matched pattern assume this is inline CQL, need to reply on
                            // cql-execution library to throw error if it is invalid
                            if (!pattern.matcher(expressionString).find()) {
                                String cqlExpression = "define " + "\"" + "linkId." + itemComponent.getLinkId()
                                        + "\"" + ": "
                                        + expressionString;
                                String elm = null;
                                try {
                                    elm = CqlExecution.translateToElm(cqlExpression, this);
                                    logger.info("converted elm: " + elm);
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                    logger.error("Failed to convert inline CQL to elm. For linkId "
                                            + itemComponent.getLinkId());
                                }
                                if (elm != null) {
                                    expression.setExpression(elm);
                                    expression.setLanguage("application/elm+json");
                                }
                            }
                        }
                    }
                }
            }

            if (itemComponent.hasItem()) {
                findAndReplaceEmbeddedCql(itemComponent.getItem());
            }
        }
    }

    private boolean hasEmbeddedCql(QuestionnaireItemComponent item) {
        List<Extension> extensionList = item.getExtension();
        // support expressions list
        // sdc-questionnaire-initialExpression,
        if (extensionList.isEmpty())
            return false;
        for (int i = 0; i < extensionList.size(); i++) {
            Extension extension = extensionList.get(i);
            if (extension.getUrl()
                    .equals("http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-initialExpression")) {
                Expression expression = (Expression) extension.getValue();
                if (expression.getLanguage().equals("text/cql")) {
                    String expressionString = expression.getExpression();
                    // regex for \"library\".statement
                    final String libraryRefRegex = "^\\\\\\\"[a-zA-Z0-9]+\\\\\\\".[a-zA-Z0-9]+$";
                    final Pattern pattern = Pattern.compile(libraryRefRegex, Pattern.MULTILINE);
                    // if not matched pattern assume this is inline CQL, need to reply on
                    // cql-execution library to throw error if it is invalid
                    if (!pattern.matcher(expressionString).find()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
        String libraryFileName = String.format("%s/%s%s.cql",
                "path", libraryIdentifier.getId(),
                libraryIdentifier.getVersion() != null ? ("-" + libraryIdentifier.getVersion()) : "");
        return QuestionnaireEmbeddedCQLProcessor.class.getResourceAsStream(libraryFileName);
    }

}
