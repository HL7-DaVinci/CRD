package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.cdshooks.AlternativeTherapy;
import org.cdshooks.CoverageRequirements;
import org.cdshooks.DrugInteraction;
import org.cdshooks.Hook;
import org.hl7.ShortNameMaps;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.components.QueryBatchRequest;
import org.hl7.davinci.endpoint.components.CardBuilder.CqlResultsForCard;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.crdhook.ConfigurationOption;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.DiscoveryExtension;
import org.hl7.davinci.r4.crdhook.orderdispatch.OrderDispatchRequest;
import org.hl7.davinci.r4.crdhook.ordersign.CrdExtensionConfigurationOptions;
import org.hl7.davinci.r4.crdhook.ordersign.CrdPrefetchTemplateElements;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.json.simple.JSONObject;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component("r4_OrderDispatchService")
public class OrderDispatchService extends CdsService<OrderDispatchRequest> {
    public static final String ID = "order-dispatch-crd";
    public static final String TITLE = "order-dispatch Coverage Requirements Discovery";
    public static final Hook HOOK = Hook.ORDER_DISPATCH;
    public static final String DESCRIPTION = "test";

    public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
            CrdPrefetchTemplateElements.COVERAGE_REQUEST_BUNDLE,
            CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
            // CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
            // CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
            CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
            CrdPrefetchTemplateElements.SERVICE_REQUEST_BUNDLE);
    // CrdPrefetchTemplateElements.MEDICATION_DISPENSE_BUNDLE);
    public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
    static final Logger logger = LoggerFactory.getLogger(OrderSignService.class);

    public static final List<ConfigurationOption> CONFIGURATION_OPTIONS = Arrays.asList(
            CrdExtensionConfigurationOptions.ALTERNATIVE_THERAPY
    );
    //public static final DiscoveryExtension EXTENSION = new DiscoveryExtension(CONFIGURATION_OPTIONS);

    public OrderDispatchService() { super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS, null); }

    @Override
    public List<CoverageRequirementRuleResult> createCqlExecutionContexts(OrderDispatchRequest request, FileStore fileStore, String baseUrl) throws RequestIncompleteException {
        return null;
    }

    @Override
    protected CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic) {
        return null;
    }

    @Override
    protected void attempQueryBatchRequest(OrderDispatchRequest request, QueryBatchRequest qbr) {

    }
}
