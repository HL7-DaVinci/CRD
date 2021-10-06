package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.cdshooks.AlternativeTherapy;
import org.cdshooks.CoverageRequirements;
import org.cdshooks.DrugInteraction;
import org.cdshooks.Hook;
import org.hl7.ShortNameMaps;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.components.CardBuilder.CqlResultsForCard;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.crdhook.ConfigurationOption;
import org.hl7.davinci.r4.crdhook.DiscoveryExtension;
import org.hl7.davinci.r4.crdhook.ordersign.CrdExtensionConfigurationOptions;
import org.hl7.davinci.r4.crdhook.ordersign.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.ordersign.OrderSignRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.json.simple.JSONObject;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component("r4_OrderSignService")
public class OrderSignService extends CdsService<OrderSignRequest> {

  public static final String ID = "order-sign-crd";
  public static final String TITLE = "order-sign Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_SIGN;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
      CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
      CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.SERVICE_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.MEDICATION_DISPENSE_BUNDLE);
  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  static final Logger logger = LoggerFactory.getLogger(OrderSignService.class);

  public static final List<ConfigurationOption> CONFIGURATION_OPTIONS = Arrays.asList(
      CrdExtensionConfigurationOptions.ALTERNATIVE_THERAPY
  );
  public static final DiscoveryExtension EXTENSION = new DiscoveryExtension(CONFIGURATION_OPTIONS);

  public OrderSignService() { super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS, EXTENSION); }

  @Override
  public List<CoverageRequirementRuleResult> createCqlExecutionContexts(OrderSignRequest orderSignRequest, FileStore fileStore, String baseUrl) {
    FhirBundleProcessor fhirBundleProcessor = new FhirBundleProcessor(orderSignRequest.getPrefetch(), fileStore, baseUrl);
    fhirBundleProcessor.processDeviceRequests();
    fhirBundleProcessor.processMedicationRequests();
    fhirBundleProcessor.processServiceRequests();
    fhirBundleProcessor.processMedicationDispenses();
    List<CoverageRequirementRuleResult> results = fhirBundleProcessor.getResults();

    if (results.isEmpty()) {
      throw RequestIncompleteException.NoSupportedBundlesFound();
    }
    return results;
  }

  protected CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic) {
    CqlResultsForCard results = new CqlResultsForCard();

    results.setRuleApplies((Boolean) evaluateStatement("RULE_APPLIES", context));
    if (!results.ruleApplies()) {
      logger.warn("rule does not apply");
      return results;
    }

    CoverageRequirements coverageRequirements = new CoverageRequirements();
    coverageRequirements.setApplies(true);

    String humanReadableTopic = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(topic), ' ');

    coverageRequirements.setInfoLink(evaluateStatement("RESULT_InfoLink", context).toString())
        .setPriorAuthRequired((Boolean) evaluateStatement("PRIORAUTH_REQUIRED", context))
        .setDocumentationRequired((Boolean) evaluateStatement("DOCUMENTATION_REQUIRED", context));

    // if prior auth, supercede the documentation required
    if (coverageRequirements.isPriorAuthRequired()) {
      logger.info("Prior Auth Required");
      coverageRequirements.setSummary(humanReadableTopic + ": Prior Authorization required.")
          .setDetails("Prior Authorization required, follow the attached link for information.");
    } else if (coverageRequirements.isDocumentationRequired()) {
      logger.info("Documentation Required");
      coverageRequirements.setSummary(humanReadableTopic + ": Documentation Required.")
          .setDetails("Documentation Required, please complete form via Smart App link.");
    } else {
      logger.info("No Prior Auth or Documentation Required");
      coverageRequirements.setSummary(humanReadableTopic + ": No Prior Authorization required.")
          .setDetails("No Prior Authorization required for " + humanReadableTopic + ".");
    }

    if (evaluateStatement("RESULT_requestId", context) != null) {
      results.setRequest((IBaseResource) evaluateStatement("RESULT_requestId", context));
      coverageRequirements.setRequestId(JSONObject.escape(fhirComponents.getFhirContext().newJsonParser()
          .encodeResourceToString(results.getRequest())));
    }

    // setup the alternative therapy information
    AlternativeTherapy alternativeTherapy = new AlternativeTherapy();
    alternativeTherapy.setApplies(false);

    if (evaluateStatement("RESULT_dispense", context) != null) {
      results.setRequest((IBaseResource) evaluateStatement("RESULT_dispense", context));
      coverageRequirements.setRequestId(JSONObject.escape(fhirComponents.getFhirContext().newJsonParser()
          .encodeResourceToString(results.getRequest())));

      // only display the dispense form for MedicationDispense request
      try {
        if (evaluateStatement("RESULT_QuestionnaireDispenseUri", context) != null) {
          coverageRequirements.setQuestionnaireDispenseUri(evaluateStatement("RESULT_QuestionnaireDispenseUri", context).toString());
        }
      } catch (Exception e) {
        logger.info("-- No Dispense questionnaire defined");
      }
    }
    else // not a MedicationDispense
    {
      if (evaluateStatement("RESULT_QuestionnaireOrderUri", context) != null) {
        coverageRequirements.setQuestionnaireOrderUri(evaluateStatement("RESULT_QuestionnaireOrderUri", context).toString());
      }

      try {
        if (evaluateStatement("RESULT_QuestionnaireFaceToFaceUri", context) != null) {
          coverageRequirements.setQuestionnaireFaceToFaceUri(evaluateStatement("RESULT_QuestionnaireFaceToFaceUri", context).toString());
        }
      } catch (Exception e) {
        logger.info("-- No face to face questionnaire defined");
      }

      try {
        if (evaluateStatement("RESULT_QuestionnaireLabUri", context) != null) {
          coverageRequirements.setQuestionnaireLabUri(evaluateStatement("RESULT_QuestionnaireLabUri", context).toString());
        }
      } catch (Exception e) {
        logger.info("-- No Lab questionnaire defined");
      }

      try {
        if (evaluateStatement("RESULT_QuestionnaireProgressNoteUri", context) != null) {
          coverageRequirements.setQuestionnaireProgressNoteUri(evaluateStatement("RESULT_QuestionnaireProgressNoteUri", context).toString());
        }
      } catch (Exception e) {
        logger.info("-- No Progress note questionnaire defined");
      }

      try {
        if (evaluateStatement("RESULT_QuestionnairePlanOfCareUri", context) != null) {
          coverageRequirements.setQuestionnairePlanOfCareUri(evaluateStatement("RESULT_QuestionnairePlanOfCareUri", context).toString());
        }
      } catch (Exception e) {
        logger.info("-- No plan of care questionnaire defined");
      }

      try {
        if (evaluateStatement("RESULT_QuestionnairePARequestUri", context) != null) {
          coverageRequirements.setQuestionnairePARequestUri(evaluateStatement("RESULT_QuestionnairePARequestUri", context).toString());
        }
      } catch (Exception e) {
        logger.info("-- No PA Request questionnaire defined");
      }

      // process the alternative therapies
      try {
        if (evaluateStatement("ALTERNATIVE_THERAPY", context) != null) {
          Object ac = evaluateStatement("ALTERNATIVE_THERAPY", context);

          Code code = (Code) ac;
          logger.info("alternate therapy suggested: " + code.getDisplay() + " (" + code.getCode() + " / " +
              ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.inverse().get(code.getSystem()).toUpperCase() + ")");

          alternativeTherapy.setApplies(true)
              .setCode(code.getCode())
              .setSystem(code.getSystem())
              .setDisplay(code.getDisplay());
        }
      } catch (Exception e) {
        logger.info("-- No alternative therapy defined");
      }
    }
    results.setCoverageRequirements(coverageRequirements);
    results.setAlternativeTherapy(alternativeTherapy);

    // add empty drug interaction
    DrugInteraction drugInteraction = new DrugInteraction();
    drugInteraction.setApplies(false);
    results.setDrugInteraction(drugInteraction);

    return results;
  }
}
