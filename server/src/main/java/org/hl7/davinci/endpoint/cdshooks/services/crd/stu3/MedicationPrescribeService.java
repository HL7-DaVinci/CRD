package org.hl7.davinci.endpoint.cdshooks.services.crd.stu3;

import org.cdshooks.CdsResponse;
import org.cdshooks.CdsService;
import org.cdshooks.Hook;
import org.cdshooks.Prefetch;
import java.util.List;
import javax.validation.Valid;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.PrefetchHydrator;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.stu3.FhirComponents;
import org.hl7.davinci.stu3.Utilities;
import org.hl7.davinci.stu3.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.davinci.stu3.crdhook.CrdPrefetchTemplateElements;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;


@Component("stu3_MedicationPrescribeService")
public class MedicationPrescribeService extends CdsService {
  static final Logger logger = LoggerFactory.getLogger(MedicationPrescribeService.class);

  public static final String ID = "medication-prescribe-crd";
  public static final String TITLE = "medication-prescribe Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.MEDICATION_PRESCRIBE;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static Prefetch PREFETCH = null;
  static {
    PREFETCH = new Prefetch();
    PREFETCH.put(CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE.getKey(),
        CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE.getQuery());
  }

  public MedicationPrescribeService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH);
  }

  /**
   * Handle the post request to the service.
   *
   * @param request The json request, parsed.
   */
  public CdsResponse handleRequest(@Valid @RequestBody MedicationPrescribeRequest request) {

    logger.info("handleRequest: start");
    logger.info(
        "Medications bundle size: " + request.getContext().getMedications().getEntry().size());

    FhirComponents fhirComponents = FhirComponents.getInstance();
    PrefetchHydrator prefetchHydrator = new PrefetchHydrator<Bundle>(this, request,
        fhirComponents.getFhirContext());
    prefetchHydrator.hydrate(); //prefetch is now as hydrated as possible

    CdsResponse response = new CdsResponse();

    Bundle medicationRequestBundle = request.getPrefetch().getMedicationRequestBundle();
    if (medicationRequestBundle == null) {
      logger.error("Prefetch medicationRequestBundle not a bundle");
      response.addCard(CardBuilder.summaryCard(
          "medicationRequestBundle could not be (pre)fetched in this request "));
      return response;
    }
    List<MedicationRequest> medicationRequestList = Utilities.getResourcesOfTypeFromBundle(
        MedicationRequest.class, (Bundle) medicationRequestBundle);

    // TODO - Replace this with database lookup logic
    response
        .addCard(CardBuilder.summaryCard("Responses from this service are currently hard coded."));
    for (MedicationRequest medicationRequest : medicationRequestList) {

      CoverageRequirementRule crr = new CoverageRequirementRule();
      crr.setAgeRangeHigh(80);
      crr.setAgeRangeLow(55);
      crr.setEquipmentCode("E0424");
      crr.setGenderCode("F".charAt(0));
      crr.setNoAuthNeeded(false);
      crr.setInfoLink("https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/"
          + "MLNProducts/Downloads/Home-Oxygen-Therapy-Text-Only.pdf");

      response.addCard(CardBuilder.transform(crr));
    }

    logger.info("handleRequest: end");
    return response;
  }

}
