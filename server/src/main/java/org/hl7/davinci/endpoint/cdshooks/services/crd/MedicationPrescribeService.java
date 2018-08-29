package org.hl7.davinci.endpoint.cdshooks.services.crd;

import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.cdshooks.CdsResponse;
import org.hl7.davinci.cdshooks.CdsService;
import org.hl7.davinci.cdshooks.Hook;
import org.hl7.davinci.cdshooks.Prefetch;
import org.hl7.davinci.cdshooks.medicationprescribe.MedicationPrescribeFetcher;
import org.hl7.davinci.cdshooks.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

@Component
public class MedicationPrescribeService extends CdsService {
  static final Logger logger = LoggerFactory.getLogger(MedicationPrescribeService.class);

  public static final String ID = "medication-prescribe-crd";
  public static final String TITLE = "medication-prescribe Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.MEDICATION_PRESCRIBE;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final Prefetch PREFETCH = null;

  public MedicationPrescribeService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH);
  }

  /**
   * Handle the post request to the service.
   * @param request The json request, parsed.
   * @return
   */
  public CdsResponse handleRequest(@Valid @RequestBody MedicationPrescribeRequest request) {

    logger.info("handleRequest: start");
    logger.info("Medications bundle size: " + request.getContext().getMedications().getEntry().size());

    MedicationPrescribeFetcher fetcher = new MedicationPrescribeFetcher(request);
    fetcher.fetch();

//    if (request.getPrefetch().getPatient() != null) {
//      logger.info("handleRequest: patient birthdate: "
//          + request.getPrefetch().getPatient().getBirthDate().toString());
//    }
//    if (request.getPrefetch().getCoverage() != null) {
//      logger.info("handleRequest: coverage id: "
//          + request.getPrefetch().getCoverage().getId());
//    }
//    if (request.getPrefetch().getLocation() != null) {
//      logger.info("handleRequest: location address: "
//          + request.getPrefetch().getLocation().getAddress().getCity() + ", "
//          + request.getPrefetch().getLocation().getAddress().getState());
//    }
//    if (request.getPrefetch().getInsurer() != null) {
//      logger.info("handleRequest: insurer id: "
//          + request.getPrefetch().getInsurer().getName());
//    }
//    if (request.getPrefetch().getProvider() != null) {
//      logger.info("handleRequest: provider name: "
//          + request.getPrefetch().getProvider().getName().get(0).getPrefixAsSingleString() + " "
//          + request.getPrefetch().getProvider().getName().get(0).getFamily());
//    }

    if (fetcher.hasRequest()) {
      List<Annotation> list = fetcher.getMedicationRequest().getNote();
      if (!list.isEmpty()) {
        logger.info("handleRequest: " + fetcher.getMedicationRequest().getNote().get(0).getText());
      } else {
        logger.info("handleRequest: no notes specified");
      }
    } else {
      // TODO: raise error
      logger.error("No request provided!");
    }

    CdsResponse response = new CdsResponse();

    // TODO - Replace this with database lookup logic
    response.addCard(CardBuilder.summaryCard("Responses from this service are currently hard coded."));

    CoverageRequirementRule crr = new CoverageRequirementRule();
    crr.setAgeRangeHigh(80);
    crr.setAgeRangeLow(55);
    crr.setEquipmentCode("E0424");
    crr.setGenderCode("F".charAt(0));
    crr.setNoAuthNeeded(false);
    crr.setInfoLink("https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/"
        + "MLNProducts/Downloads/Home-Oxygen-Therapy-Text-Only.pdf");

    response.addCard(CardBuilder.transform(crr));
    logger.info("handleRequest: end");
    return response;
  }
}
