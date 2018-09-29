package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.Utilities;
import org.cdshooks.CdsResponse;
import org.cdshooks.CdsService;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.CrdPrefetchTemplateElements;
import org.cdshooks.Hook;
import org.cdshooks.Prefetch;
import org.hl7.davinci.r4.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.PrefetchHydrator;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleFinder;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Component("r4_MedicationPrescribeService")
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
    List<MedicationRequest> medicationRequestList = Utilities.getResourcesOfTypeFromBundle(
        MedicationRequest.class, (Bundle) medicationRequestBundle);

    for (MedicationRequest medicationRequest : medicationRequestList) {

      Patient patient = null;
      CodeableConcept cc = null;
      try {
        if (medicationRequest.hasMedicationCodeableConcept()) {
          cc = medicationRequest.getMedicationCodeableConcept();
        }
        if (medicationRequest.hasMedicationReference()) {
          Medication med = (Medication) medicationRequest.getMedicationReference().getResource();
          cc = med.getCode();
        }
      } catch (FHIRException fe) {
        response
            .addCard(CardBuilder.summaryCard("Unable to parse the medication code out of the request"));
      }

      try {
        patient = (Patient) medicationRequest.getSubject().getResource();
      } catch (Exception e) {
        response
            .addCard(CardBuilder.summaryCard("No patient could be (pre)fetched in this request"));
      }

      if (patient != null && cc != null) {
        int patientAge = Utilities.calculateAge(patient);
        List<CoverageRequirementRule> coverageRequirementRules = new ArrayList();
        for (Coding c : cc.getCoding()) {
          List<CoverageRequirementRule> r = ruleFinder
              .findRules(patientAge, patient.getGender(), c.getCode(),
                  c.getSystem());
          if (r.size() > 0) {
            coverageRequirementRules.addAll(r);
          }
        }
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
