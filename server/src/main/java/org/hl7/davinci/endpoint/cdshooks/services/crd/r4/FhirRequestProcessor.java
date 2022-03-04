package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import org.cdshooks.AlternativeTherapy;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.JsonElement;

public class FhirRequestProcessor {

  static final Logger logger = LoggerFactory.getLogger(FhirRequestProcessor.class);

  private static final String REFERENCE = "reference";

  public static IBaseResource swapTherapyInRequest(IBaseResource request, AlternativeTherapy alternativeTherapy) {
    IBaseResource output = request;

    // build a CodeableConcept with the new code
    CodeableConcept codeableConcept = new CodeableConcept();
    List<Coding> codingList = new ArrayList<>();
    Coding code = new Coding();
    code.setCode(alternativeTherapy.getCode())
        .setDisplay(alternativeTherapy.getDisplay())
        .setSystem(alternativeTherapy.getSystem());
    codingList.add(code);
    codeableConcept.setCoding(codingList);

    // convert the request into real object type
    switch (request.fhirType()) {
      case "DeviceRequest":
        DeviceRequest deviceRequest = ((DeviceRequest) request).copy();
        deviceRequest.setCode(codeableConcept);
        deviceRequest.setId(new IdType()); // clear the ID
        deviceRequest.setIdentifier(new ArrayList<>()); // clear the identifier
        deviceRequest.setMeta(new Meta()); // clear the meta
        deviceRequest.setAuthoredOn(new Date()); // clear the authored date
        output = deviceRequest;
        break;
      case "MedicationRequest":
        MedicationRequest medicationRequest = ((MedicationRequest) request).copy();
        medicationRequest.setMedication(codeableConcept);
        medicationRequest.setId(new IdType()); // clear the ID
        medicationRequest.setIdentifier(new ArrayList<>()); // clear the identifier
        medicationRequest.setMeta(new Meta()); // clear the meta
        medicationRequest.setAuthoredOn(new Date()); // clear the authored date
        output = medicationRequest;
        break;
      case "MedicationDispense":
        MedicationDispense medicationDispense = ((MedicationDispense) request).copy();
        medicationDispense.setMedication(codeableConcept);
        medicationDispense.setId(new IdType()); // clear the ID
        medicationDispense.setIdentifier(new ArrayList<>()); // clear the identifier
        medicationDispense.setMeta(new Meta()); // clear the meta
        output = medicationDispense;
        break;
      case "ServiceRequest":
        ServiceRequest serviceRequest = ((ServiceRequest) request).copy();
        serviceRequest.setCode(codeableConcept);
        serviceRequest.setId(new IdType()); // clear the ID
        serviceRequest.setIdentifier(new ArrayList<>()); // clear the identifier
        serviceRequest.setMeta(new Meta()); // clear the meta
        serviceRequest.setAuthoredOn(new Date()); // clear the authored date
        output = serviceRequest;
        break;
      case "SupplyRequest":
        SupplyRequest supplyRequest = ((SupplyRequest) request).copy();
        supplyRequest.setItem(codeableConcept);
        supplyRequest.setId(new IdType()); // clear the ID
        supplyRequest.setIdentifier(new ArrayList<>()); // clear the identifier
        supplyRequest.setMeta(new Meta()); // clear the meta
        supplyRequest.setAuthoredOn(new Date()); // clear the authored date
        output = supplyRequest;
        break;
      case "NutritionOrder":
      case "Appointment":
      case "Encounter":
      default:
        logger.info("Unsupported fhir R4 resource type (" + request.fhirType() + ") when swapping therapy");
        throw new RuntimeException("Unsupported fhir R4 resource type " + request.fhirType());
    }

    return output;
  }

  public static IBaseResource addNoteToRequest(IBaseResource request, Annotation note) {
    IBaseResource output = request;

    switch (request.fhirType()) {
      case "DeviceRequest":
        DeviceRequest deviceRequest = ((DeviceRequest) request).copy();
        deviceRequest.addNote(note);
        output = deviceRequest;
        break;
      case "MedicationRequest":
        MedicationRequest medicationRequest = ((MedicationRequest) request).copy();
        medicationRequest.addNote(note);
        output = medicationRequest;
        break;
      case "MedicationDispense":
        MedicationDispense medicationDispense = ((MedicationDispense) request).copy();
        medicationDispense.addNote(note);
        output = medicationDispense;
        break;
      case "ServiceRequest":
        ServiceRequest serviceRequest = ((ServiceRequest) request).copy();
        serviceRequest.addNote(note);
        output = serviceRequest;
        break;
      case "NutritionOrder":
        NutritionOrder nutritionOrder = ((NutritionOrder) request).copy();
        nutritionOrder.addNote(note);
        output = nutritionOrder;
        break;
      case "SupplyRequest":
      case "Appointment":
      case "Encounter":
      default:
        logger.info("Unsupported fhir R4 resource type (" + request.fhirType() + ") when adding note");
        throw new RuntimeException("Unsupported fhir R4 resource type " + request.fhirType());
    }

    return output;
  }

  public static IBaseResource addSupportingInfoToRequest(IBaseResource request, Reference reference) {
    IBaseResource output = request;

    switch (request.fhirType()) {
      case "DeviceRequest":
        DeviceRequest deviceRequest = ((DeviceRequest) request).copy();
        deviceRequest.addSupportingInfo(reference);
        output = deviceRequest;
        break;
      case "MedicationRequest":
        MedicationRequest medicationRequest = ((MedicationRequest) request).copy();
        medicationRequest.addSupportingInformation(reference);
        output = medicationRequest;
        break;
      case "MedicationDispense":
        MedicationDispense medicationDispense = ((MedicationDispense) request).copy();
        medicationDispense.addSupportingInformation(reference);
        output = medicationDispense;
        break;
      case "ServiceRequest":
        ServiceRequest serviceRequest = ((ServiceRequest) request).copy();
        serviceRequest.addSupportingInfo(reference);
        output = serviceRequest;
        break;
      case "Appointment":
        Appointment appointment = ((Appointment) request).copy();
        appointment.addSupportingInformation(reference);
        output = appointment;
        break;
      case "NutritionOrder":
      case "SupplyRequest":
      case "Encounter":
      default:
        logger.info("Unsupported fhir R4 resource type (" + request.fhirType() + ") when adding note");
        throw new RuntimeException("Unsupported fhir R4 resource type " + request.fhirType());
    }

    return output;
  }

  /**
   * Adds the given resource to the given CrdPrefetch based on the given resource
   * type.
   * 
   * @param crdResponse
   * @param resource
   * @param requestType
   */
  public static void addToCrdPrefetchRequest(CrdPrefetch crdResponse, ResourceType requestType,
      List<BundleEntryComponent> resourcesToAdd) {
    switch (requestType) {
      case DeviceRequest:
        if (crdResponse.getDeviceRequestBundle() == null) {
          crdResponse.setDeviceRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdResponse.getDeviceRequestBundle(), resourcesToAdd);
        break;
      case MedicationRequest:
        if (crdResponse.getMedicationRequestBundle() == null) {
          crdResponse.setMedicationRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdResponse.getMedicationRequestBundle(), resourcesToAdd);
        break;
      case NutritionOrder:
        if (crdResponse.getNutritionOrderBundle() == null) {
          crdResponse.setNutritionOrderBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdResponse.getNutritionOrderBundle(), resourcesToAdd);
        break;
      case ServiceRequest:
        if (crdResponse.getServiceRequestBundle() == null) {
          crdResponse.setServiceRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdResponse.getServiceRequestBundle(), resourcesToAdd);
        break;
      case SupplyRequest:
        if (crdResponse.getSupplyRequestBundle() == null) {
          crdResponse.setSupplyRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdResponse.getSupplyRequestBundle(), resourcesToAdd);
        break;
      case Appointment:
        if (crdResponse.getAppointmentBundle() == null) {
          crdResponse.setAppointmentBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdResponse.getAppointmentBundle(), resourcesToAdd);
        break;
      case Encounter:
        if (crdResponse.getEncounterBundle() == null) {
          crdResponse.setEncounterBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdResponse.getEncounterBundle(), resourcesToAdd);
        break;
      case MedicationDispense:
        if (crdResponse.getDeviceRequestBundle() == null) {
          crdResponse.setDeviceRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdResponse.getMedicationDispenseBundle(), resourcesToAdd);
        break;
      case MedicationStatement:
        if (crdResponse.getMedicationStatementBundle() == null) {
          crdResponse.setMedicationStatementBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdResponse.getMedicationStatementBundle(), resourcesToAdd);
        break;
      default:
        throw new RuntimeException("Unexpected resource type for draft order request. Given " + requestType + ".");
    }
  }

  /**
   * Adds non-duplicate resources that do not already exist in the bundle to the bundle.
   */
  private static void addNonDuplicateResourcesToBundle(Bundle bundle, List<BundleEntryComponent> resourcesToAdd) {
    for (BundleEntryComponent resourceEntry : resourcesToAdd) {
      if (!bundle.getEntry().stream()
          .anyMatch(bundleEntry -> bundleEntry.getResource().getId().equals(resourceEntry.getResource().getId()))) {
        bundle.addEntry(resourceEntry);
      }
    }
  }

  public static List<Patient> extractPatientsFromBundle(Bundle queryResponseBundle) {
    List<Patient> coverages = new ArrayList<>();
    for (BundleEntryComponent entry : queryResponseBundle.getEntry()) {
      if (entry.getResource().getResourceType().equals(ResourceType.Patient)) {
        coverages.add((Patient) entry.getResource());
      }
    }
    return coverages;
  }

  public static List<Coverage> extractCoverageFromBundle(Bundle queryResponseBundle) {
    List<Coverage> coverages = new ArrayList<>();
    for (BundleEntryComponent entry : queryResponseBundle.getEntry()) {
      if (entry.getResource().getResourceType().equals(ResourceType.Coverage)) {
        coverages.add((Coverage) entry.getResource());
      }
    }
    return coverages;
  }

  /**
   * Extracts the reference Ids from the given JSON.
   * 
   * @param references
   * @param jsonElement
   */
  public static void extractReferenceIds(List<String> references, JsonElement jsonElement) {
    if (jsonElement.isJsonArray()) {
      for (JsonElement innerElement : jsonElement.getAsJsonArray()) {
        extractReferenceIds(references, innerElement);
      }
    } else if (jsonElement.isJsonObject()) {
      if (jsonElement.getAsJsonObject().has(REFERENCE)) {
        if (jsonElement.getAsJsonObject().get(REFERENCE).isJsonObject()) {
          String referenceId = jsonElement.getAsJsonObject().get(REFERENCE).getAsJsonObject()
              .get("myStringValue").toString();
          references.add(referenceId.replace("\"", ""));
        }
      }
    }
  }

  /**
   * Adds the given coverage and patient to the given resource.
   * @param resource  The resource to add coverage and patient data to.
   * @param patients  The list of valid patients.
   * @param coverages The list of valid coverages.
   */
  public static void addInsuranceAndSubject(Resource resource, List<Patient> patients, List<Coverage> coverages) {
    // Source: https://build.fhir.org/ig/HL7/davinci-crd/hooks.html#prefetch
    switch(resource.getResourceType()){
      case Appointment:
        // Appointment does not define an insurance or subject field.
      case DeviceRequest:
        DeviceRequest deviceRequest = (DeviceRequest) resource;
        if(!coverages.isEmpty()){
          deviceRequest.getInsuranceFirstRep().setResource(coverages.get(0));
        }
        if(!patients.isEmpty()){
          deviceRequest.getSubject().setResource(patients.get(0));
        }
        break;
      case Encounter:
        Encounter encounter = (Encounter) resource;
        // Encounter does not have an insurance field.
        if(!patients.isEmpty()){
          encounter.getSubject().setResource(patients.get(0));
        }
        break;
      case MedicationRequest:
        MedicationRequest medicationRequest = (MedicationRequest) resource;
        if(!coverages.isEmpty()){
          medicationRequest.getInsuranceFirstRep().setResource(coverages.get(0));
        }
        if(!patients.isEmpty()){
          medicationRequest.getSubject().setResource(patients.get(0));
        }
      case MedicationDispense:
        MedicationDispense dispense = (MedicationDispense) resource;
        // MedicationDispense does not have an insurance field.
        // It is also not defined in the prefetch section of the spec, though that is likely a typo from the duplicated MedicationRequest.
        if(!patients.isEmpty()){
          dispense.getSubject().setResource(patients.get(0));
        }
        break;
      case NutritionOrder:
        // Appointment does not define an insurance or subject field.
      case ServiceRequest:
        ServiceRequest serviceRequest = (ServiceRequest) resource;
        if(!coverages.isEmpty()){
          serviceRequest.getInsuranceFirstRep().setResource(coverages.get(0));
        }
        if(!patients.isEmpty()){
          serviceRequest.getSubject().setResource(patients.get(0));
        }
        break;
      default:
        // The input request type is not one of the 7 defined above and in the spec.
        throw new RuntimeException("Invalid request type: " + resource.getResourceType() + ".");
    }
  }
}
