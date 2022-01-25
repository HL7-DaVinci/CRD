package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import org.cdshooks.AlternativeTherapy;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FhirRequestProcessor {

  static final Logger logger = LoggerFactory.getLogger(FhirRequestProcessor.class);

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
}
