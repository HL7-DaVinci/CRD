package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import org.cdshooks.Hook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.QueryBatchRequest;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.crdhook.ConfigurationOption;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.DiscoveryExtension;
import org.hl7.davinci.r4.crdhook.appointmentbook.CrdExtensionConfigurationOptions;
import org.hl7.davinci.r4.crdhook.appointmentbook.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.encounterstart.EncounterStartRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.engine.execution.Context;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component("r4_EncounterStartService")
public class EncounterStartService extends CdsService<EncounterStartRequest> {

    public static final String ID = "encounter-start-crd";
    public static final String TITLE = "encounter-start Coverage Requirements Discovery";
    public static final Hook HOOK = Hook.APPOINTMENT_BOOK;
    public static final String DESCRIPTION =
            "Get information regarding the coverage requirements for encounters";
    public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
            CrdPrefetchTemplateElements.COVERAGE_REQUEST_BUNDLE,
            CrdPrefetchTemplateElements.PATIENT,
            CrdPrefetchTemplateElements.ENCOUNTER_BUNDLE);
    public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
    public static final List<ConfigurationOption> CONFIGURATION_OPTIONS = Arrays.asList(
            CrdExtensionConfigurationOptions.COVERAGE,
            CrdExtensionConfigurationOptions.MAX_CARDS
    );
    public static final DiscoveryExtension EXTENSION = new DiscoveryExtension(CONFIGURATION_OPTIONS);

    public EncounterStartService() { super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS, EXTENSION);}

    @Override
    public List<CoverageRequirementRuleResult> createCqlExecutionContexts(EncounterStartRequest request, FileStore fileStore, String baseUrl) throws RequestIncompleteException {
        //    List<String> selections = Arrays.asList(request.getContext().getSelections());
        List<String> selections = null;

        FhirBundleProcessor fhirBundleProcessor = new FhirBundleProcessor(fileStore, baseUrl, selections);
        CrdPrefetch prefetch = request.getPrefetch();
        //It should be safe to cast these as Bundles as any OperationOutcome's found in the prefetch that could not get resolved would have thrown an exception
        List<CoverageRequirementRuleResult> results = fhirBundleProcessor.getResults();

        if (results.isEmpty()) {
            throw RequestIncompleteException.NoSupportedBundlesFound();
        }
        return results;
    }

    @Override
    protected CardBuilder.CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic) {
        CardBuilder.CqlResultsForCard cardResult = new CardBuilder.CqlResultsForCard();
        cardResult.setRequest((IBaseResource)context);
        return cardResult;
    }

    @Override
    protected void attemptQueryBatchRequest(EncounterStartRequest request, QueryBatchRequest qbr) {

    }
}
