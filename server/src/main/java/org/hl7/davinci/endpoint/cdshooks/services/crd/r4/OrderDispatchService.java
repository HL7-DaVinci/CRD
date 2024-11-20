package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.ArrayList;
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
import org.hl7.davinci.endpoint.database.RuleMapping;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.crdhook.ConfigurationOption;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.DiscoveryExtension;
import org.hl7.davinci.r4.crdhook.orderdispatch.OrderDispatchContext;
import org.hl7.davinci.r4.crdhook.orderdispatch.OrderDispatchRequest;
import org.hl7.davinci.r4.crdhook.ordersign.CrdExtensionConfigurationOptions;
import org.hl7.davinci.r4.crdhook.ordersign.CrdPrefetchTemplateElements;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
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
    private static final String USAGE_REQUIREMENTS = "String patientid, array dispatchedOrders, String performer";
    public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
            CrdPrefetchTemplateElements.COVERAGE_REQUEST_BUNDLE,
            CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
            // CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
            //  CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
            CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
            CrdPrefetchTemplateElements.SERVICE_REQUEST_BUNDLE);
    // CrdPrefetchTemplateElements.MEDICATION_DISPENSE_BUNDLE);
    public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
    static final Logger logger = LoggerFactory.getLogger(OrderSignService.class);

    public static final List<ConfigurationOption> CONFIGURATION_OPTIONS = Arrays.asList(
            CrdExtensionConfigurationOptions.ALTERNATIVE_THERAPY,
            CrdExtensionConfigurationOptions.DTR_CLIN,
            CrdExtensionConfigurationOptions.PRIOR_AUTH,
            CrdExtensionConfigurationOptions.COVERAGE,
            CrdExtensionConfigurationOptions.MAX_CARDS
    );
    public static final DiscoveryExtension EXTENSION = new DiscoveryExtension(CONFIGURATION_OPTIONS);

    public OrderDispatchService() { super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS, EXTENSION, USAGE_REQUIREMENTS); }

    @Override
    public List<CoverageRequirementRuleResult> createCqlExecutionContexts(OrderDispatchRequest request, FileStore fileStore, String baseUrl) throws RequestIncompleteException {
        FhirBundleProcessor fhirBundleProcessor = new FhirBundleProcessor(fileStore, baseUrl);
        CrdPrefetch prefetch = request.getPrefetch();
        Bundle coverageBundle = (Bundle) prefetch.getCoverageBundle();

        // Process DeviceRequests
        if (prefetch.getDeviceRequestBundle() != null) {
            fhirBundleProcessor.processDeviceRequests((Bundle) prefetch.getDeviceRequestBundle(), coverageBundle);
        }

        // Process MedicationRequests
        if (prefetch.getMedicationRequestBundle() != null) {
            fhirBundleProcessor.processMedicationRequests((Bundle) prefetch.getMedicationRequestBundle(), coverageBundle);
        }

        // Process ServiceRequests
        if (prefetch.getServiceRequestBundle() != null) {
            fhirBundleProcessor.processServiceRequests((Bundle) prefetch.getServiceRequestBundle(), coverageBundle);
        }

        List<CoverageRequirementRuleResult> results = fhirBundleProcessor.getResults();
        if (results.isEmpty()) {
            throw RequestIncompleteException.NoSupportedBundlesFound();
        }
        return results;
    }

    @Override
    protected CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic) {
        CqlResultsForCard results = new CqlResultsForCard();

        results.setRuleApplies((Boolean) evaluateStatement("RULE_APPLIES", context));
        if (!results.ruleApplies()) {
            logger.warn("Rule does not apply.");
            return results;
        }

        CoverageRequirements coverageRequirements = new CoverageRequirements();
        coverageRequirements.setApplies(true);

        String humanReadableTopic = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(topic), ' ');

        coverageRequirements.setInfoLink(evaluateStatement("RESULT_InfoLink", context).toString());
        coverageRequirements.setPriorAuthRequired((Boolean) evaluateStatement("PRIORAUTH_REQUIRED", context));
        coverageRequirements.setDocumentationRequired((Boolean) evaluateStatement("DOCUMENTATION_REQUIRED", context));

        // Add additional logic for Prior Auth or Documentation Required
        if (coverageRequirements.isPriorAuthRequired()) {
            coverageRequirements.setSummary(humanReadableTopic + ": Prior Authorization required.")
                    .setDetails("Prior Authorization required, follow the attached link for information.");
        } else if (coverageRequirements.isDocumentationRequired()) {
            coverageRequirements.setSummary(humanReadableTopic + ": Documentation Required.")
                    .setDetails("Documentation Required, please complete form via Smart App link.");
        } else {
            coverageRequirements.setSummary(humanReadableTopic + ": No Prior Authorization required.")
                    .setDetails("No Prior Authorization required for " + humanReadableTopic + ".");
        }

        results.setCoverageRequirements(coverageRequirements);
        return results;
    }

    @Override
    protected void attemptQueryBatchRequest(OrderDispatchRequest request, QueryBatchRequest qbr) {
        try {
            logger.info("Attempting Query Batch Request for OrderDispatch.");
            qbr.performDispatchQueryBatchRequest(request, request.getPrefetch());
        } catch (Exception e) {
            logger.error("Failed to perform query batch request: {}", e.getMessage(), e);
        }
    }
}
