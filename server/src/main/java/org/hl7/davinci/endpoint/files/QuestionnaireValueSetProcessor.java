package org.hl7.davinci.endpoint.files;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes FHIR R4 Questionnaires to have referenced valuesets embedded.
 */
public class QuestionnaireValueSetProcessor extends FhirResourceProcessor<Questionnaire> {

  static final Logger logger = LoggerFactory.getLogger(QuestionnaireValueSetProcessor.class);
  static final String ValueSetReferenceExtensionUrl = "http://hl7.org/fhir/us/davinci-dtr/StructureDefinition/valueset-reference";

  /**
   * Processes the Questionnaire to have referenced ValueSets included in the contains field.
   */
	@Override
	protected Questionnaire processResource(Questionnaire inputResource, FileStore fileStore, String baseUrl) {

    // Initialize map of created urls to valuesets to embed
    Map<String, ValueSet> valueSetMap = new HashMap<String, ValueSet>();

    // Iterate through items recursively and replace answerValueSet appropiately
    findAndReplaceValueSetReferences(inputResource.getItem(), valueSetMap, fileStore, baseUrl);

    // Add all loaded valuesets to the contains field
    for (ValueSet valueSet : valueSetMap.values()) {
      inputResource.addContained(valueSet);

      // START WORKAROUNDS for HAPI Issue: https://github.com/jamesagnew/hapi-fhir/issues/1184
      // Add extension that references the contained ValueSet due to HAPI encoder error.
      // This is needed to make sure the referenced ValueSets in contains are included.
      inputResource.addExtension(new Extension(ValueSetReferenceExtensionUrl, new Reference(valueSet.getUrl())));
      // END WORKAROUNDS

      logger.info("Embedding " + valueSet.getId() + " in contained.");
    }

		return inputResource;
  }

  /**
   * Recursively visits every questionnaire item and replaces every `answerValueSet` that isn't a local
   * hash (#) reference into a local reference. This fills the valueSetMap with the loaded valuesets.
   * 
   * @param itemComponents The item components to visit.
   * @param valueSetMap A mapping of ValueSet urls to loaded valuesets that should be filled as references are found.
   * @param fileStore The file store that is used to load valuesets from.
   * @param baseUrl The base url of the server from the request. Used to identify local valuesets.
   */
  private void findAndReplaceValueSetReferences(List<QuestionnaireItemComponent> itemComponents,
    Map<String, ValueSet> valueSetMap, FileStore fileStore, String baseUrl) {

    for (QuestionnaireItemComponent itemComponent : itemComponents) {
      // If there is an answerValueSet field we need to do some work on this item
      if (itemComponent.hasAnswerValueSet()) {

        // Only look for a valueset to embed if it does not appear to be a hash reference
        if (!itemComponent.getAnswerValueSet().startsWith("#")) {
          logger.info("answerValueSet found with url - " + itemComponent.getAnswerValueSet());
          String valueSetId = findAndLoadValueSet(itemComponent.getAnswerValueSet(), valueSetMap, fileStore, baseUrl);
          if (valueSetId != null) {
            itemComponent.getAnswerValueSet();
            itemComponent.setAnswerValueSet("#" + valueSetId);
            logger.info("answerValueSet replaced with  - " + itemComponent.getAnswerValueSet());
          } else {
            logger.warn("Referenced ValueSet: " + itemComponent.getAnswerValueSet() + " was not found.");
          }
        }
      }

      // Recurse down into child items.
      if (itemComponent.hasItem()) {
        findAndReplaceValueSetReferences(itemComponent.getItem(), valueSetMap, fileStore, baseUrl);
      }
    }
  }

  /**
   * Finds a value set, loads it and modifies its id and url fields to work in the contains field.
   * 
   * @param url The canonical url of the valueset to look for.
   * @param valueSetMap The map of valuesets that have been loaded already.
   * @return The local ID to use for the valueset. null if valueset wasn't found.
   */
  private String findAndLoadValueSet(String url, Map<String, ValueSet> valueSetMap, FileStore fileStore, String baseUrl) {
    if (valueSetMap.containsKey(url)) {
      return valueSetMap.get(url).getId();
    }

    FileResource valueSetFileResource;
    ValueSet valueSet;
    // If URL starts with this server's base url, pull out id and search by id
    if (url.startsWith(baseUrl)) {
      String valueSetId = url.split("ValueSet/")[1];
      valueSetFileResource = fileStore.getFhirResourceById("R4", "valueset", valueSetId, baseUrl);
    } else {
      valueSetFileResource = fileStore.getFhirResourceByUrl("R4", "valueset", url, baseUrl);
    }

    if (valueSetFileResource != null) {
      // parse value set and modify ID and #URL to match.
      valueSet = (ValueSet) this.parseFhirFileResource(valueSetFileResource);
      String valueSetId = valueSet.getIdElement().getIdPart();
      valueSet.setId(valueSetId);
      valueSet.setUrl("#" + valueSetId);

      // add it to the value set map so it can be reused
      valueSetMap.put(url, valueSet);
      return valueSetId;
    } else {
      return null;
    }
  }
}
