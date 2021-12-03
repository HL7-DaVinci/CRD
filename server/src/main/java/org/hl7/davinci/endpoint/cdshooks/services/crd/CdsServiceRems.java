package org.hl7.davinci.endpoint.cdshooks.services.crd;

import org.cdshooks.Card;
import org.cdshooks.CdsRequest;
import org.cdshooks.CdsResponse;
import org.cdshooks.Hook;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.r4.crdhook.DiscoveryExtension;
import org.hl7.fhir.r4.model.Coding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public abstract class CdsServiceRems<requestTypeT extends CdsRequest<?, ?>> extends CdsAbstract<requestTypeT> {
    static final Logger logger = LoggerFactory.getLogger(CdsServiceRems.class);

    public List<Coding> remsDrugs = new ArrayList<>();

    public CdsServiceRems(String id, Hook hook, String title, String description,
                      List<PrefetchTemplateElement> prefetchElements, FhirComponentsT fhirComponents,
                      DiscoveryExtension extension) {
        super(id, hook, title, description, prefetchElements, fhirComponents, extension);
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

    /**
     * Performs generic operations for incoming requests of any type.
     *
     * @param request the generically typed incoming request
     * @return The response from the server
     */
    public CdsResponse handleRequest(@Valid @RequestBody requestTypeT request, URL applicationBaseUrl) {
        CdsResponse response = new CdsResponse();
        List<Coding> medications = getMedications(request);
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

    private boolean isRemsDrug(Coding medication) {
        for( Coding remsDrug : remsDrugs){
            if (remsDrug.getCode().equals(medication.getCode())){
                return true;
            }
        }
        return false;
    }

    public abstract List<Coding> getMedications(requestTypeT request);

}