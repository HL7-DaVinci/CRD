package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.cdshooks.*;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.components.CardBuilder.CqlResultsForCard;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.crdhook.orderselect.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.orderselect.OrderSelectRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Coding;
import org.json.simple.JSONObject;
import org.opencds.cqf.cql.engine.execution.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;


@Component("r4_OrderSelectServiceRems")
public class OrderSelectServiceRems extends CdsService<OrderSelectRequest> {

    @Autowired
    FileStore fileStore;

    public static final String ID = "order-select-rems";
    public static final String TITLE = "order-select Coverage Requirements Discovery";
    public static final Hook HOOK = Hook.ORDER_SELECT;
    public static final String DESCRIPTION =
            "Get information regarding the coverage requirements for durable medical equipment";
    public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
            CrdPrefetchTemplateElements.MEDICATION_STATEMENT_BUNDLE,
            CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE);
    public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
    static final Logger logger = LoggerFactory.getLogger(OrderSelectService.class);

    public OrderSelectServiceRems() { super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS); }

//    @Override
//    public CdsResponse handleRequest(@Valid @RequestBody OrderSelectRequest request, URL applicationBaseUrl) {
//        CdsResponse response = new CdsResponse();
//        List<String> selections = Arrays.asList(request.getContext().getSelections());
//        List<CoverageRequirementRuleResult> results = createCqlExecutionContexts(request, fileStore, applicationBaseUrl.toString() + "/");
//        response = isRemsDrug(results.get(0).getCriteria().getCode());
//        return response;
//    }
    @Override
    public List<CoverageRequirementRuleResult> createCqlExecutionContexts(OrderSelectRequest orderSelectRequest, FileStore fileStore, String baseUrl) {
        List<String> selections = Arrays.asList(orderSelectRequest.getContext().getSelections());
        System.out.println(selections);
        FhirBundleProcessor fhirBundleProcessor = new FhirBundleProcessor(orderSelectRequest.getPrefetch(), fileStore, baseUrl, selections);
        fhirBundleProcessor.processMedicationRequests();
        List<CoverageRequirementRuleResult> results = fhirBundleProcessor.getResults();
        System.out.println(results.size());

        if (results.isEmpty()) {
            throw RequestIncompleteException.NoSupportedBundlesFound();
        }

        return results;
    }

    protected CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic) {
        CqlResultsForCard results = new CqlResultsForCard();
        results.setRuleApplies((Boolean) evaluateStatement("RULE_APPLIES", context));
        System.out.println(results.ruleApplies());
        if (!results.ruleApplies()) {
            return results;
        }

        CoverageRequirements coverageRequirements = new CoverageRequirements();
        coverageRequirements.setApplies(true);
        coverageRequirements.setSummary("This is a rems drug");
        coverageRequirements.setPriorAuthRequired(true);
        results.setRequest((IBaseResource) evaluateStatement("RESULT_requestId", context));
        coverageRequirements.setRequestId(JSONObject.escape(fhirComponents.getFhirContext().newJsonParser()
                .encodeResourceToString(results.getRequest())));
        results.setCoverageRequirements(coverageRequirements);

        AlternativeTherapy alternativeTherapy = new AlternativeTherapy();
        alternativeTherapy.setApplies(false);
        results.setAlternativeTherapy(alternativeTherapy);

        DrugInteraction drugInteraction = new DrugInteraction();
        drugInteraction.setApplies(false);
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
}
