package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import org.cdshooks.Hook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.endpoint.Utils;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsServiceRems;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.orderselect.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.orderselect.OrderSelectRequest;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Component("r4_OrderSelectServiceRems")
public class OrderSelectServiceRems extends CdsServiceRems<OrderSelectRequest> {

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
    }


    public List<Coding> getMedications(OrderSelectRequest request) {
        List<String> selections = Arrays.asList(request.getContext().getSelections());
        CrdPrefetch prefetch = request.getPrefetch();
        List<Coding> medications = getSelections(prefetch, selections);
        return medications;
    }
    public List<Coding> getSelections(CrdPrefetch prefetch, List<String> selections) {
        Bundle medicationRequestBundle = prefetch.getMedicationRequestBundle();
        List<MedicationRequest> medicationRequestList = Utilities.getResourcesOfTypeFromBundle(MedicationRequest.class, medicationRequestBundle);
        List<Coding> codings = new ArrayList<>();

        if (!medicationRequestList.isEmpty()) {
            for (MedicationRequest medicationRequest : medicationRequestList) {
                if (Utils.idInSelectionsList(medicationRequest.getId(), selections)) {
                    codings.add(medicationRequest.getMedicationCodeableConcept().getCodingFirstRep()); // assume there is only 1 coding per MR
                }
            }
        }

        return codings;
    }

}
