package org.hl7.davinci.endpoint.files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hl7.fhir.r4.model.Resource;

public class SubQuestionnaireProcessor extends FhirResourceProcessor<Questionnaire> {

  static final Logger logger = LoggerFactory.getLogger(SubQuestionnaireProcessor.class);

	@Override
	protected Questionnaire processResource(Questionnaire inputResource, FileStore fileStore, String baseUrl) {
		return this.assembleQuestionnaire(inputResource, fileStore, baseUrl);
	}
  

  protected Questionnaire assembleQuestionnaire(Questionnaire q, FileStore fileStore, String baseUrl) {
    logger.info("SubQuestionnaireProcessor::assembleQuestionnaire(): " + q.getId());


    List<Extension> extensionList = q.getExtension();
    Hashtable<String, org.hl7.fhir.r4.model.Resource> containedList = new Hashtable<String, org.hl7.fhir.r4.model.Resource>();

    for (org.hl7.fhir.r4.model.Resource r : q.getContained()) {
      containedList.put(r.getId(), r);
    }

    int containedSize = containedList.size();

    parseItemList(q.getItem(), fileStore, baseUrl, containedList, extensionList);
    
    if (containedSize != containedList.size())
      q.setContained(new ArrayList<org.hl7.fhir.r4.model.Resource>(containedList.values()));

    return q;
  }

  private void parseItemList(List<QuestionnaireItemComponent> itemList, FileStore fileStore, String baseUrl,
    Hashtable<String, org.hl7.fhir.r4.model.Resource> containedList, List<Extension> extensionList) {
    if (itemList == null || itemList.size() == 0)
      return;

    for (int i = 0; i < itemList.size();) {
      List<QuestionnaireItemComponent> returnedItemList = 
        parseItem(itemList.get(i), fileStore, baseUrl, containedList, extensionList);
      
      if (returnedItemList.size() == 0) {
        continue;
      }

      if (returnedItemList.size() == 1) {
        itemList.set(i, returnedItemList.get(0));
      }
      else {
        itemList.remove(i);
        itemList.addAll(i, returnedItemList);
      }

      i += returnedItemList.size();
    }
  }

  private List<QuestionnaireItemComponent> parseItem(QuestionnaireItemComponent item, FileStore fileStore, String baseUrl,
  Hashtable<String, org.hl7.fhir.r4.model.Resource> containedList, List<Extension> extensionList) {
    // find if item has an extension is sub-questionnaire
    Extension e = item.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/sub-questionnaire");

    if (e != null) {
      // read sub questionnaire from file store
      CanonicalType value = e.castToCanonical(e.getValue());
      logger.info("SubQuestionnaireProcessor::parseItem(): Looking for SubQuestionnaire " + value);
      FileResource subFileResource = fileStore.getFhirResourceById("R4", "questionnaire", value.asStringValue(), baseUrl, false);
      Questionnaire subQuestionnaire = (Questionnaire) this.parseFhirFileResource(subFileResource);

      if (subQuestionnaire != null) {
        // merge extensions
        for (Extension subExtension : subQuestionnaire.getExtension()) {
          if (extensionList.stream().noneMatch(ext -> ext.equalsDeep(subExtension))) {
            extensionList.add(subExtension);
          }
        }

        // merge contained resources
        for (org.hl7.fhir.r4.model.Resource r : subQuestionnaire.getContained()) {
          containedList.put(r.getId(), r);
        }

        return subQuestionnaire.getItem();

      } else {
        // SubQuestionnaire was not found
        logger.warn("SubQuestionnaireProcessor::parseItem(): Could not find " + value);
        return Arrays.asList(item);
      }
    }
    
    // parse sub-items
    this.parseItemList(item.getItem(), fileStore, baseUrl, containedList, extensionList);

    return Arrays.asList(item);
  }
}