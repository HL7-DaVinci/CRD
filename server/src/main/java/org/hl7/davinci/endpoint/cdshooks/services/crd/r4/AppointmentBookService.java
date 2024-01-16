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
import org.hl7.davinci.r4.crdhook.DiscoveryExtension;
import org.opencds.cqf.cql.engine.execution.Context;
import org.springframework.stereotype.Component;
import org.hl7.davinci.r4.crdhook.appointmentbook.AppointmentBookRequest;
import org.hl7.davinci.r4.crdhook.appointmentbook.CrdPrefetchTemplateElements;
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
		      CrdPrefetchTemplateElements.PATIENT_BUNDLE,
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void attemptQueryBatchRequest(AppointmentBookRequest request, QueryBatchRequest qbr) {
		// TODO Auto-generated method stub
		
	}

}
