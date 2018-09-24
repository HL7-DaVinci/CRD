package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import org.cdshooks.CdsRequest;
import org.cdshooks.Hook;
import org.cdshooks.Prefetch;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleFinder;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("r4_MedicationPrescribeService")
public class MedicationPrescribeService
    extends CdsService<Bundle, MedicationRequest, Patient, CodeableConcept> {

  public static final String ID = "medication-prescribe-crd";
  public static final String TITLE = "medication-prescribe Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.MEDICATION_PRESCRIBE;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final Prefetch PREFETCH;
  public static final Logger logger = LoggerFactory.getLogger(MedicationPrescribeService.class);
  public static final String FHIRVERSION = "r4";
  static {
    PREFETCH = new Prefetch();
    PREFETCH.put(CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE.getKey(),
        CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE.getQuery());
  }

  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  @Autowired
  CoverageRequirementRuleFinder ruleFinder;

  public MedicationPrescribeService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH, FHIRCOMPONENTS, FHIRVERSION);
  }
  public CodeableConcept getCc(MedicationRequest medicationRequest) throws FHIRException {
    return medicationRequest.getMedicationCodeableConcept();
  }
  public Patient getPatient(MedicationRequest medicationRequest) {
    return (Patient) medicationRequest.getSubject().getResource();
  }

  @Override
  public List<MedicationRequest> getRequests(CdsRequest request) {
    MedicationPrescribeRequest medicationPrescribeRequest = (MedicationPrescribeRequest) request;
    Bundle medicationRequestBundle = medicationPrescribeRequest.getPrefetch().getMedicationRequestBundle();
    Utilities util = new Utilities();
    return util.getResourcesOfTypeFromBundle(MedicationRequest.class, medicationRequestBundle);
  }
}
