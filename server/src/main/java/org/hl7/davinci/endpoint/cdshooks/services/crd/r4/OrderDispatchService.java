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

    public OrderDispatchService() { super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS, null, USAGE_REQUIREMENTS); }

    @Override
    public List<CoverageRequirementRuleResult> createCqlExecutionContexts(OrderDispatchRequest request, FileStore fileStore, String baseUrl) throws RequestIncompleteException {
        List<CoverageRequirementRuleResult> ruleResults = new ArrayList<>();
        OrderDispatchContext context = request.getContext();

        // Retrieve identifiers
        String performer = context.getPerformer();
        String orderId = context.getOrder();

        if (orderId != null && !orderId.isEmpty()) {
            // Define the criteria for retrieving rules based on the context
            CoverageRequirementRuleCriteria criteria = new CoverageRequirementRuleCriteria()
                    .setPayor(performer)
                    .setCode("someCodeValue") // Replace with appropriate code
                    .setCodeSystem("someCodeSystem") // Replace with appropriate code system
                    .setFhirVersion("R4"); // Specify the FHIR version as needed

            // Retrieve matching rules from FileStore
            List<RuleMapping> rules = fileStore.findRules(criteria);
            if (rules != null && !rules.isEmpty()) {
                for (RuleMapping rule : rules) {
                    CoverageRequirementRuleResult ruleResult = new CoverageRequirementRuleResult();
                    ruleResult.setCriteria(criteria);
                    ruleResult.setTopic(rule.getTopic());
                    ruleResults.add(ruleResult);
                }
            } else {
                // If no rule is found, generate a CoverageRequirementRuleResult manually
                CoverageRequirementRuleResult ruleResult = new CoverageRequirementRuleResult()
                        .setCriteria(criteria)
                        .setTopic("No rule found for given criteria"); // Set based on your use case
                ruleResults.add(ruleResult);
            }
        } else {
            throw new RequestIncompleteException("Order ID is missing in the context.");
        }

        return ruleResults;
    }

    @Override
    protected CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic) {
        CqlResultsForCard cardResults = new CqlResultsForCard();
        try {
            Object result = context.resolveExpressionRef(topic).evaluate(context);

            // Use instanceof to determine the type of result and set appropriate fields
            if (result instanceof CoverageRequirements) {
                cardResults.setCoverageRequirements((CoverageRequirements) result);
            } else if (result instanceof AlternativeTherapy) {
                cardResults.setAlternativeTherapy((AlternativeTherapy) result);
            } else if (result instanceof DrugInteraction) {
                cardResults.setDrugInteraction((DrugInteraction) result);
            } else {
                logger.warn("Unexpected CQL result type: {}", result.getClass().getName());
            }

        } catch (Exception e) {
            logger.error("Error executing CQL for topic " + topic, e);
        }
        return cardResults;
    }

    @Override
    protected void attemptQueryBatchRequest(OrderDispatchRequest request, QueryBatchRequest qbr) {
        if (StringUtils.isNotBlank(request.getContext().getPatientId()) && request.getPrefetch() != null) {
            logger.info("Attempting Query Batch Request for OrderDispatch.");
            try {
                // Use performQueryBatchRequest to backfill the CRD response
                qbr.performQueryBatchRequest(request, request.getPrefetch());
            } catch (Exception e) {
                logger.error("Failed to perform query batch request: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("Skipping Query Batch Request: Patient ID or prefetch data is missing.");
        }
    }
}
