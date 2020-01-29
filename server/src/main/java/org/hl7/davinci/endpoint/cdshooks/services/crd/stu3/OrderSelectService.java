package org.hl7.davinci.endpoint.cdshooks.services.crd.stu3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cdshooks.Hook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleFinder;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.stu3.FhirComponents;
import org.hl7.davinci.stu3.crdhook.CrdPrefetch;
import org.hl7.davinci.stu3.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.stu3.crdhook.orderselect.OrderSelectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component("stu3_OrderSelectService")
public class OrderSelectService extends CdsService<OrderSelectRequest>  {

  public static final String ID = "order-select-crd";
  public static final String TITLE = "order-select Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_SELECT;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  static final Logger logger = LoggerFactory.getLogger(OrderSelectService.class);
  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
        CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
        CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.PROCEDURE_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.REFERRAL_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.VISION_PRESCRIPTION_BUNDLE
    );

  public OrderSelectService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS);
  }

  @Override
  public List<CoverageRequirementRuleResult> createCqlExecutionContexts(OrderSelectRequest orderSelectRequest, CoverageRequirementRuleFinder ruleFinder) {

    // Note: the selections array is currently ignored, all of the draftOrders are processed, not just those selected.
    FhirBundleProcessor fhirBundleProcessor = new FhirBundleProcessor(orderSelectRequest.getPrefetch(), ruleFinder);
    fhirBundleProcessor.processDeviceRequests();
    fhirBundleProcessor.processMedicationRequests();
    List<CoverageRequirementRuleResult> results = fhirBundleProcessor.getResults();

    if (results.isEmpty()) {
      throw RequestIncompleteException.NoSupportedBundlesFound();
    }
    return results;
  }
}
