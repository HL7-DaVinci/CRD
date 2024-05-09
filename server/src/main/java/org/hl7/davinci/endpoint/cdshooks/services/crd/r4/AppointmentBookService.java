package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.Arrays;
import java.util.List;

import org.cdshooks.Hook;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.components.CardBuilder.CqlResultsForCard;
import org.hl7.davinci.endpoint.components.QueryBatchRequest;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.crdhook.ConfigurationOption;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.DiscoveryExtension;
import org.opencds.cqf.cql.engine.execution.Context;
import org.springframework.stereotype.Component;
import org.hl7.davinci.r4.crdhook.appointmentbook.AppointmentBookContext;
import org.hl7.davinci.r4.crdhook.appointmentbook.AppointmentBookRequest;
import org.hl7.davinci.r4.crdhook.appointmentbook.CrdPrefetchTemplateElements;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.davinci.r4.crdhook.appointmentbook.CrdExtensionConfigurationOptions;

@Component("r4_AppointmentBookService")
public class AppointmentBookService extends CdsService<AppointmentBookRequest>{

	public static final String ID = "appointment-book-crd";
	public static final String TITLE = "appointment-book Coverage Requirements Discovery";
	public static final Hook HOOK = Hook.APPOINTMENT_BOOK;
	public static final String DESCRIPTION =
			"Get information regarding the coverage requirements for appointments";
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


	public AppointmentBookService() {
		super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS, EXTENSION);
	}

	@Override
	public List<CoverageRequirementRuleResult> createCqlExecutionContexts(AppointmentBookRequest request,
																		  FileStore fileStore, String baseUrl) throws RequestIncompleteException {
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
	protected	 CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic) {
		// TODO Auto-generated method stub
		CqlResultsForCard cardResult = new CqlResultsForCard();
		//AppointmentBookContext request = new AppointmentBookContext();
		//AppointmentBookRequest request =
		cardResult.setRequest((IBaseResource)context);
		return cardResult;
	}

	@Override
	protected void attemptQueryBatchRequest(AppointmentBookRequest request, QueryBatchRequest qbr) {
		try {
			qbr.performQueryBatchRequest(request, request.getPrefetch());
		}
		catch(Exception e) {
			throw new Error("Failed to perform query batch request");
		}
	}

}
