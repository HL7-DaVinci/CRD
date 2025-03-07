package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.cdshooks.AlternativeTherapy;
import org.cdshooks.CoverageRequirements;
import org.cdshooks.DrugInteraction;
import org.cdshooks.Hook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.components.CardBuilder.CqlResultsForCard;
import org.hl7.davinci.endpoint.components.QueryBatchRequest;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.crdhook.ConfigurationOption;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.DiscoveryExtension;
import org.hl7.davinci.r4.crdhook.orderselect.CrdExtensionConfigurationOptions;
import org.hl7.davinci.r4.crdhook.orderselect.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.orderselect.OrderSelectRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.json.simple.JSONObject;
import org.opencds.cqf.cql.engine.execution.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component("r4_OrderSelectService")
public class OrderSelectService extends CdsService<OrderSelectRequest> {

  public static final String ID = "order-select-crd";
  public static final String TITLE = "order-select Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_SELECT;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  private static final String USAGE_REQUIREMENTS = "String userId, String patientid, array selections, object draftODers";
  public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
      CrdPrefetchTemplateElements.COVERAGE_PREFETCH_QUERY,
      CrdPrefetchTemplateElements.MEDICATION_STATEMENT_BUNDLE,
      CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE);
  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  static final Logger logger = LoggerFactory.getLogger(OrderSelectService.class);

  public static final List<ConfigurationOption> CONFIGURATION_OPTIONS = Arrays.asList(
          CrdExtensionConfigurationOptions.COVERAGE,
          CrdExtensionConfigurationOptions.MAX_CARDS
  );
  public static final DiscoveryExtension EXTENSION = new DiscoveryExtension(CONFIGURATION_OPTIONS);

  public OrderSelectService() { super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS, EXTENSION, USAGE_REQUIREMENTS); }

  @Override
  public List<CoverageRequirementRuleResult> createCqlExecutionContexts(OrderSelectRequest orderSelectRequest, FileStore fileStore, String baseUrl) {

    List<String> selections = Arrays.asList(orderSelectRequest.getContext().getSelections());

    FhirBundleProcessor fhirBundleProcessor = new FhirBundleProcessor(fileStore, baseUrl, selections);
    CrdPrefetch prefetch = orderSelectRequest.getPrefetch();
    //It should be safe to cast these as Bundles as any OperationOutcome's found in the prefetch that could not get resolved would have thrown an exception
    fhirBundleProcessor.processOrderSelectMedicationStatements((Bundle)prefetch.getMedicationRequestBundle(), (Bundle)prefetch.getMedicationStatementBundle(), (Bundle)prefetch.getCoverageBundle());
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
      return results;
    }

    CoverageRequirements coverageRequirements = new CoverageRequirements();
    coverageRequirements.setApplies(false);

    if (evaluateStatement("RESULT_requestId", context) != null) {
      results.setRequest((IBaseResource) evaluateStatement("RESULT_requestId", context));
      coverageRequirements.setRequestId(JSONObject.escape(fhirComponents.getFhirContext().newJsonParser()
          .encodeResourceToString(results.getRequest())));
    }

    results.setCoverageRequirements(coverageRequirements);

    AlternativeTherapy alternativeTherapy = new AlternativeTherapy();
    alternativeTherapy.setApplies(false);
    results.setAlternativeTherapy(alternativeTherapy);

    DrugInteraction drugInteraction = new DrugInteraction();
    drugInteraction.setApplies(false);
    try {
      if (evaluateStatement("DRUG_INTERACTION", context) != null) {
        drugInteraction.setApplies((Boolean) evaluateStatement("DRUG_INTERACTION", context));

        if (drugInteraction.getApplies()) {
          drugInteraction.setSummary("WARNING! Drug Interaction Found!");
          String detail = "Drug ";

          if (evaluateStatement("REQUESTED_DRUG_CODE", context) != null) {
            Coding code = getFirstCodeFromCodingListObject(evaluateStatement("REQUESTED_DRUG_CODE", context));
            detail = detail + " " + code.getDisplay() + " (" + code.getCode() + ") has a dangerous drug/drug interaction with medication patient is already taking: ";
          }
          if (evaluateStatement("STATEMENT_DRUG_CODE", context) != null) {
            Coding code = getFirstCodeFromCodingListObject(evaluateStatement("STATEMENT_DRUG_CODE", context));
            detail = detail + code.getDisplay() + " (" + code.getCode() + ")";
          }

          drugInteraction.setDetail(detail);
        }
      }
    } catch (Exception e) {
      logger.info("-- No drug interaction defined");
    }
    results.setDrugInteraction(drugInteraction);

    return results;
  }

  private Coding getFirstCodeFromCodingListObject(Object c) {
    List<?> clist = new ArrayList<>();
    if (c instanceof Collection) {
      clist = new ArrayList<>((Collection<?>) c);
    }
    List<Coding> codingList = new ArrayList<>();
    for (Object obj: clist) {
      codingList.add((Coding) obj);
    }
    return codingList.get(0);
  }

  @Override
  protected void attemptQueryBatchRequest(OrderSelectRequest request, QueryBatchRequest batchRequest) {
    batchRequest.performDraftOrderQueryBatchRequest(request, request.getPrefetch());
  }
}
