package org.hl7.davinci.endpoint.cdshooks.services.crd.stu3;

import java.util.List;
import javax.validation.Valid;
import org.hl7.davinci.stu3.FhirComponents;
import org.hl7.davinci.stu3.Utilities;
import org.cdshooks.CdsResponse;
import org.cdshooks.CdsService;
import org.hl7.davinci.stu3.crdhook.CrdPrefetch;
import org.hl7.davinci.stu3.crdhook.CrdPrefetchTemplateElements;
import org.cdshooks.Hook;
import org.cdshooks.Prefetch;
import org.hl7.davinci.stu3.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.PrefetchHydrator;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleFinder;
import org.hl7.davinci.stu3.fhirresources.DaVinciMedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Component("stu3_MedicationPrescribeService")
public class MedicationPrescribeService extends CdsService {

  public static final String ID = "medication-prescribe-crd";
  public static final String TITLE = "medication-prescribe Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.MEDICATION_PRESCRIBE;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final Prefetch PREFETCH;
  static final Logger logger = LoggerFactory.getLogger(MedicationPrescribeService.class);

  static {
    PREFETCH = new Prefetch();
    PREFETCH.put(CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE.getKey(),
        CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE.getQuery());
  }


  @Autowired
  CoverageRequirementRuleFinder ruleFinder;

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
    if (request.getPrefetch() == null)
      request.setPrefetch(new CrdPrefetch());
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
    List medicationRequestList = Utilities.getResourcesOfTypeFromBundle(
        DaVinciMedicationRequest.class, medicationRequestBundle);

    if (medicationRequestList.isEmpty()) {
      logger.warn("Unable to find any profiled MedicationRequests, looking for standard ones.");
      medicationRequestList = Utilities.getResourcesOfTypeFromBundle(
          MedicationRequest.class, medicationRequestBundle);
    }

    for (Object mr : medicationRequestList) {
      MedicationRequest medicationRequest = (MedicationRequest) mr;
      Patient patient = null;
      CodeableConcept cc = null;
      try {
        cc = medicationRequest.getMedicationCodeableConcept();
      } catch (FHIRException fe) {
        response
            .addCard(CardBuilder.summaryCard("Unable to parse the medication code out of the request"));
      }

      // See if the patient is in the prefetch
      try {
        patient = (Patient) medicationRequest.getSubject().getResource();
      } catch (Exception e) {
        response
            .addCard(CardBuilder.summaryCard("No patient could be (pre)fetched in this request"));
      }

      if (patient != null && cc != null) {
        int patientAge = Utilities.calculateAge(patient);
        List<CoverageRequirementRule> coverageRequirementRules = ruleFinder
            .findRules(patientAge, patient.getGender(), cc.getCoding().get(0).getCode(),
                cc.getCoding().get(0).getSystem());
        if (coverageRequirementRules.size() == 0) {
          response.addCard(CardBuilder.summaryCard("No documentation rules found"));
        } else {
          for (CoverageRequirementRule rule: coverageRequirementRules) {
            response.addCard(CardBuilder.transform(rule));
          }
        }
      }

    }
    CardBuilder.errorCardIfNonePresent(response);
    logger.info("handleRequest: end");
    return response;
  }
}

