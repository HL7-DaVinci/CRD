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
   * Adds the given resource to the given CrdPrefetch based on the given resource type.
   * @param crdResponse
   * @param resource
   * @param requestType
   */
  public static void addToCrdPrefetchRequest(CrdPrefetch crdResponse, ResourceType requestType, List<BundleEntryComponent> resources) {
    switch (requestType) {
      case DeviceRequest:
        if(crdResponse.getDeviceRequestBundle() == null){
          Bundle deviceRequestBundle = new Bundle();
          deviceRequestBundle.setType(BundleType.COLLECTION);
          deviceRequestBundle.getTypeElement().setValue(BundleType.COLLECTION);
          deviceRequestBundle.getTypeElement().addExtension();
          crdResponse.setDeviceRequestBundle(deviceRequestBundle);
        }
        addNonDuplicateResourcesToBundle(crdResponse.getDeviceRequestBundle(), resources);
        break;
      case MedicationRequest:
        addNonDuplicateResourcesToBundle(crdResponse.getMedicationRequestBundle(), resources);
        break;
      case NutritionOrder:
        addNonDuplicateResourcesToBundle(crdResponse.getNutritionOrderBundle(), resources);
        break;
      case ServiceRequest:
        if(crdResponse.getServiceRequestBundle() == null){
          crdResponse.setServiceRequestBundle(new Bundle());
        }
        addNonDuplicateResourcesToBundle(crdResponse.getServiceRequestBundle(), resources);
        break;
      case SupplyRequest:
        addNonDuplicateResourcesToBundle(crdResponse.getSupplyRequestBundle(), resources);
        break;
      case Appointment:
        addNonDuplicateResourcesToBundle(crdResponse.getAppointmentBundle(), resources);
        break;
      case Encounter:
        addNonDuplicateResourcesToBundle(crdResponse.getEncounterBundle(), resources);
        break;
      case MedicationDispense:
        addNonDuplicateResourcesToBundle(crdResponse.getMedicationDispenseBundle(), resources);
        break;
      case MedicationStatement:
        addNonDuplicateResourcesToBundle(crdResponse.getMedicationStatementBundle(), resources);
        break;
      default:
        throw new RuntimeException("Unexpected resource type for draft order request. Given " + requestType + ".");
    }
  }

  private static void addNonDuplicateResourcesToBundle(Bundle bundle, List<BundleEntryComponent> resources) {
    for(BundleEntryComponent resourceEntry : resources){
      if(!bundle.getEntry().stream().anyMatch(requestEntry -> requestEntry.getResource().getId().equals(resourceEntry.getResource().getId()))){
        bundle.addEntry(resourceEntry);
      }
    }
    bundle.setUserData("ca.uhn.fhir.parser.BaseParser_RESOURCE_CREATED_BY_PARSER", true);
    // IIdType idtype = new IdType();
    // bundle.setId(idtype);
  }

  public static List<Patient> extractPatients(Bundle queryResponseBundle) {
    List<Patient> coverages = new ArrayList<>();
    for(BundleEntryComponent entry : queryResponseBundle.getEntry()){
      if(entry.getResource().getResourceType().equals(ResourceType.Patient)){
        coverages.add((Patient) entry.getResource());
      }
    }
    return coverages;
  }

  public static List<Coverage> extractCoverage(Bundle queryResponseBundle) {
    List<Coverage> coverages = new ArrayList<>();
    for(BundleEntryComponent entry : queryResponseBundle.getEntry()){
      if(entry.getResource().getResourceType().equals(ResourceType.Coverage)){
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
}
