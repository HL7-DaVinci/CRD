package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.cdshooks.Hook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.ordersign.OrderSignRequest;
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

  public OrderSignService() { super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS); }

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
}
