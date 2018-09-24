package org.hl7.davinci.endpoint.cdshooks.services.crd.stu3;

import org.cdshooks.CdsRequest;
import org.cdshooks.Hook;
import org.cdshooks.Prefetch;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleFinder;
import org.hl7.davinci.stu3.FhirComponents;
import org.hl7.davinci.stu3.Utilities;
import org.hl7.davinci.stu3.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.stu3.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.davinci.stu3.fhirresources.DaVinciMedicationRequest;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.exceptions.FHIRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("stu3_MedicationPrescribeService")
public class MedicationPrescribeService extends
    CdsService<Bundle, DaVinciMedicationRequest, Patient, CodeableConcept>  {

  public static final String ID = "medication-prescribe-crd";
  public static final String TITLE = "medication-prescribe Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.MEDICATION_PRESCRIBE;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final Prefetch PREFETCH;
  static final Logger logger = LoggerFactory.getLogger(MedicationPrescribeService.class);
  static final String FHIRVERSION = "stu3";
  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  static {
    PREFETCH = new Prefetch();
    PREFETCH.put(CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE.getKey(),
        CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE.getQuery());
  }


  @Autowired
  CoverageRequirementRuleFinder ruleFinder;

  public MedicationPrescribeService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH, FHIRCOMPONENTS,FHIRVERSION);
  }

  public CodeableConcept getCc(DaVinciMedicationRequest medicationRequest) throws FHIRException {
    return medicationRequest.getMedicationCodeableConcept();
  }
  public Patient getPatient(DaVinciMedicationRequest medicationRequest) {
    return (Patient) medicationRequest.getSubject().getResource();
  }

  @Override
  public List<DaVinciMedicationRequest> getRequests(CdsRequest request) {
    MedicationPrescribeRequest medicationPrescribeRequest = (MedicationPrescribeRequest) request;
    Bundle medicationRequestBundle = medicationPrescribeRequest.getPrefetch().getMedicationRequestBundle();
    Utilities util = new Utilities();
    return util.getResourcesOfTypeFromBundle(DaVinciMedicationRequest.class, medicationRequestBundle);
  }
}

