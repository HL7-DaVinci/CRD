package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import org.cdshooks.Card;
import org.cdshooks.CdsResponse;
import org.cdshooks.Hook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.CardBuilder.CqlResultsForCard;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.orderselect.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.orderselect.OrderSelectRequest;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.opencds.cqf.cql.engine.execution.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Component("r4_OrderSelectServiceRems")
public class OrderSelectServiceRems extends CdsService<OrderSelectRequest> {

    @Autowired
    FileStore fileStore;

    public static final String ID = "order-select-rems";
    public static final String TITLE = "order-select-rems Coverage Requirements Discovery";
    public static final Hook HOOK = Hook.ORDER_SELECT;
    public static final String DESCRIPTION =
            "Get information regarding the coverage requirements for REMS drugs";
    public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
            CrdPrefetchTemplateElements.MEDICATION_STATEMENT_BUNDLE,
            CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE);
    public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
    static final Logger logger = LoggerFactory.getLogger(OrderSelectServiceRems.class);

    public List<Coding> remsDrugs = new ArrayList<>();

    public OrderSelectServiceRems() {
        super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS, null);

        Coding turalio = new Coding()
                .setCode("2183126")
                .setSystem("http://www.nlm.nih.gov/research/umls/rxnorm")
                .setDisplay("Turalio");
        Coding iPledge = new Coding()
                .setCode("6064")
                .setSystem("http://www.nlm.nih.gov/research/umls/rxnorm")
                .setDisplay("Isotretinoin");
        Coding revlimid = new Coding()
                .setCode("337535")
                .setSystem("http://www.nlm.nih.gov/research/umls/rxnorm")
                .setDisplay("Revlimid");
        Coding abstral = new Coding()
                .setCode("1053648")
                .setSystem("http://www.nlm.nih.gov/research/umls/rxnorm")
                .setDisplay("Abstral");
        remsDrugs.add(turalio);
        remsDrugs.add(iPledge);
        remsDrugs.add(revlimid);
        remsDrugs.add(abstral);
    }

    @Override
    public CdsResponse handleRequest(@Valid @RequestBody OrderSelectRequest request, URL applicationBaseUrl) {
        CdsResponse response = new CdsResponse();
        List<String> selections = Arrays.asList(request.getContext().getSelections());
        CrdPrefetch prefetch = request.getPrefetch();
        List<Coding> medications = getSelections(prefetch, selections);
        for (Coding medication : medications) {
            Card card = CardBuilder.summaryCard("");
            if (isRemsDrug(medication)) {
                card.setSummary(String.format("%s is a REMS drug", medication.getDisplay()));
            } else {
                card.setSummary(String.format("%s is not a REMS drug", medication.getDisplay()));
            }
            response.addCard(card);
        }
        return response;
    }

    // TODO: Change hard-coded drug checking
    private boolean isRemsDrug(Coding medication) {
        for( Coding remsDrug : remsDrugs){
            if (remsDrug.getCode().equals(medication.getCode())){
                return true;
            }
        }
        return false;
    }
    // TODO: This function is direct from FhirBundleProcessor, it should be moved to a util class
    private boolean idInSelectionsList(String identifier, List<String> selections) {
        if (selections.isEmpty()) {
            return true;
        } else {
            for ( String selection : selections) {
                if (identifier.contains(stripResourceType(selection))) {
                    return true;
                }
            }
            return false;
        }
    }

    // TODO: This function is direct from FhirBundleProcessor, it should be moved to a util class
    private String stripResourceType(String identifier) {
        int indexOfDivider = identifier.indexOf('/');
        if (indexOfDivider+1 == identifier.length()) {
            // remove the trailing '/'
            return identifier.substring(0, indexOfDivider);
        } else {
            return identifier.substring(indexOfDivider+1);
        }
    }

    public List<Coding> getSelections(CrdPrefetch prefetch, List<String> selections) {
        Bundle medicationRequestBundle = prefetch.getMedicationRequestBundle();
        List<MedicationRequest> medicationRequestList = Utilities.getResourcesOfTypeFromBundle(MedicationRequest.class, medicationRequestBundle);
        List<Coding> codings = new ArrayList<>();

        if (!medicationRequestList.isEmpty()) {
            for (MedicationRequest medicationRequest : medicationRequestList) {
                if (idInSelectionsList(medicationRequest.getId(), selections)) {
                    codings.add(medicationRequest.getMedicationCodeableConcept().getCodingFirstRep()); // assume there is only 1 coding per MR
                }
            }
        }

        return codings;
    }

    // This function could be removed, need to evaluate if we should make this class not an extension of CdsService
    @Override
    public List<CoverageRequirementRuleResult> createCqlExecutionContexts(OrderSelectRequest orderSelectRequest, FileStore fileStore, String baseUrl) {
        return new ArrayList<>();
    }

    // This function could be removed, need to evaluate if we should make this class not an extension of CdsService
    protected CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic) {
        return new CqlResultsForCard();
    }
}
