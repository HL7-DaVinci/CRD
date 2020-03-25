package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.Arrays;
import java.util.List;
import org.cdshooks.Hook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.orderreview.OrderReviewRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component("r4_OrderReviewService")
public class OrderReviewService extends CdsService<OrderReviewRequest> {

  public static final String ID = "order-review-crd";
  public static final String TITLE = "order-review Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_REVIEW;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
      CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
      CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.SERVICE_REQUEST_BUNDLE);
  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  static final Logger logger = LoggerFactory.getLogger(OrderReviewService.class);

  public OrderReviewService() { super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS); }

  @Override
  public List<CoverageRequirementRuleResult> createCqlExecutionContexts(OrderReviewRequest orderReviewRequest, FileStore fileStore, String baseUrl) {

    FhirBundleProcessor fhirBundleProcessor = new FhirBundleProcessor(orderReviewRequest.getPrefetch(), fileStore, baseUrl);
    fhirBundleProcessor.processDeviceRequests();
    fhirBundleProcessor.processMedicationRequests();
    fhirBundleProcessor.processServiceRequests();
    List<CoverageRequirementRuleResult> results = fhirBundleProcessor.getResults();

    if (results.isEmpty()) {
      throw RequestIncompleteException.NoSupportedBundlesFound();
    }
    return results;
  }

}
