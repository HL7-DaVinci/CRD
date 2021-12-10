package org.hl7.davinci.endpoint.files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes FHIR R4 questionnaire to assemble sub-questionnaires into a complete questionnaire.
 */
public class SubQuestionnaireProcessor extends FhirResourceProcessor<Questionnaire> {

  static final Logger logger = LoggerFactory.getLogger(SubQuestionnaireProcessor.class);

  /**
   * Processes a Questionnaire to replace all sub-questionnaire references with the items.
   */
	@Override
	protected Questionnaire processResource(Questionnaire inputResource, FileStore fileStore, String baseUrl) {
		return this.assembleQuestionnaire(inputResource, fileStore, baseUrl);
	}
  
  /**
   * Assemble a questionnaire to have all sub-questionnaires pulled into this questionnaire.
   * 
   * @param q The Questionnaire to assemble.
   * @param fileStore The FileStore to be used for fetching sub-questionnaires.
   * @param baseUrl The base url from the server.
   * @return The assembled questionnaire.
   */
  protected Questionnaire assembleQuestionnaire(Questionnaire q, FileStore fileStore, String baseUrl) {
    logger.info("SubQuestionnaireProcessor::assembleQuestionnaire(): " + q.getId());


    List<Extension> extensionList = q.getExtension();
    Hashtable<String, org.hl7.fhir.r4.model.Resource> containedList = new Hashtable<String, org.hl7.fhir.r4.model.Resource>();

    for (org.hl7.fhir.r4.model.Resource r : q.getContained()) {
      containedList.put(r.getId(), r);
    }

    int containedSize = containedList.size();

    processItemList(q.getItem(), fileStore, baseUrl, containedList, extensionList);
    
    if (containedSize != containedList.size())
      q.setContained(new ArrayList<org.hl7.fhir.r4.model.Resource>(containedList.values()));

    return q;
  }

  /**
   * Iterate over and modify a list of questionnaire items. This will up date the list of items as sub-questionnaire references are found.
   * 
   * @param itemList The Item list to iterate over.
   * @param fileStore The FileStore to be used for fetching sub-questionnaires.
   * @param baseUrl The base url from the server.
   * @param containedList List of contained resources to put in the assembled Questionnaire. This will be filled while iterating.
   * @param extensionList List of extensions to put in the assembled Questionnaire. This will be filled while iterating.
   */
  private void processItemList(List<QuestionnaireItemComponent> itemList, FileStore fileStore, String baseUrl,
    Hashtable<String, org.hl7.fhir.r4.model.Resource> containedList, List<Extension> extensionList) {
    if (itemList == null || itemList.size() == 0)
      return;

    for (int i = 0; i < itemList.size();) {
      List<QuestionnaireItemComponent> returnedItemList = 
        processItem(itemList.get(i), fileStore, baseUrl, containedList, extensionList);
      
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

  /**
   * Determines if this item is a sub-questionnaire reference and returns the items to replace it with. Returns the same list
   * otherwise. Also recursively continues scanning if this is just a grouping item.
   * 
   * @param item The item to check for sub-questionnaire referece.
   * @param fileStore The FileStore to be used for fetching sub-questionnaires.
   * @param baseUrl The base url from the server.
   * @param containedList List of contained resources to put in the assembled Questionnaire. This will be filled while iterating.
   * @param extensionList List of extensions to put in the assembled Questionnaire. This will be filled while iterating.
   * @return New list of items to replace this item with.
   */
  private List<QuestionnaireItemComponent> processItem(QuestionnaireItemComponent item, FileStore fileStore, String baseUrl,
  Hashtable<String, org.hl7.fhir.r4.model.Resource> containedList, List<Extension> extensionList) {
    // find if item has an extension is sub-questionnaire
    Extension e = item.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/sub-questionnaire");

    if (e != null) {
      // read sub questionnaire from file store
      CanonicalType value = e.castToCanonical(e.getValue());
      logger.info("SubQuestionnaireProcessor::parseItem(): Looking for SubQuestionnaire " + value);

      // strip the type off of the id if it is there
      String id = value.asStringValue();
      String[] parts = id.split("/");
      if (parts.length > 1) {
        id = parts[1];
      }

      boolean expandRootItem = false;
      Extension expand = item.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/sub-questionnaire-expand");

      if (expand != null) {
        expandRootItem = expand.castToBoolean(expand.getValue()).booleanValue();
      }

      FileResource subFileResource = fileStore.getFhirResourceById("R4", "questionnaire", id, baseUrl, false);
      if (subFileResource != null) {
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

          List<QuestionnaireItemComponent> rootItems = subQuestionnaire.getItem();

          // there are more than one root items in sub questionnaire, don't expand
          if (!expandRootItem || rootItems.size() > 1) {
            return rootItems;
          } else {
            return rootItems.get(0).getItem();
          }
        } else {
          // SubQuestionnaire could not be loaded
          logger.warn("SubQuestionnaireProcessor::parseItem(): Could not load SubQuestionnaire " + value.asStringValue());
          return Arrays.asList(item);
        }
      } else {
        // SubQuestionnaire could not be found
        logger.warn("SubQuestionnaireProcessor::parseItem(): Could not find SubQuestionnaire " + value.asStringValue());
        return Arrays.asList(item);
      }
    }
    
    // parse sub-items
    this.processItemList(item.getItem(), fileStore, baseUrl, containedList, extensionList);

    return Arrays.asList(item);
  }
}