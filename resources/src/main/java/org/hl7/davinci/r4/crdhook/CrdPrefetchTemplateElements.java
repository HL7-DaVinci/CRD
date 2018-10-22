package org.hl7.davinci.r4.crdhook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.fhir.r4.model.Bundle;

/**
 * Class that contains the different prefetch template elements used in crd requests.
 */
public class CrdPrefetchTemplateElements {

  public static final PrefetchTemplateElement DEVICE_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "deviceRequestBundle",
      "DeviceRequest?_id={{context.orders.DeviceRequest.id}}"
          + "&_include=DeviceRequest:patient"
          + "&_include=DeviceRequest:performer"
          + "&_include=DeviceRequest:requester"
          + "&_include=DeviceRequest:device"
          + "&_include=PractitionerRole:organization"
          + "&_include=PractitionerRole:practitioner"
          + "&_include:recurse=PractitionerRole:location"
          + "&_include:iterate=PractitionerRole:location"
          + "&_include=DeviceRequest:insurance:Coverage",
      Bundle.class);

  public static final PrefetchTemplateElement MEDICATION_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "medicationRequestBundle",
      "MedicationRequest?_id={{context.orders.MedicationRequest.id}}"
          + "&_include=MedicationRequest:patient"
          + "&_include=MedicationRequest:intended-dispenser"
          + "&_include=MedicationRequest:requester:PractitionerRole"
          + "&_include=MedicationRequest:medication"
          + "&_include=PractitionerRole:organization"
          + "&_include=PractitionerRole:practitioner"
          + "&_include=MedicationRequest:insurance:Coverage",
      Bundle.class);

  public static final PrefetchTemplateElement NUTRITION_ORDER_BUNDLE = new PrefetchTemplateElement(
      "nutritionOrderBundle",
      "NutritionOrder?_id={{context.orders.NutritionOrder.id}}"
          + "&_include=NutritionOrder:patient"
          + "&_include=NutritionOrder:provider"
          + "&_include=NutritionOrder:requester"
          + "&_include=PractitionerRole:organization"
          + "&_include=PractitionerRole:practitioner"
          + "&_include=NutritionOrder:encounter"
          + "&_include=Encounter:location"
          + "&_include=NutritionOrder:insurance:Coverage",
      Bundle.class);

  public static final PrefetchTemplateElement SERVICE_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "serviceRequestBundle",
      "ServiceRequest?_id={{context.orders.ServiceRequest.id}}"
          + "&_include=ServiceRequest:patient"
          + "&_include=ServiceRequest:performer"
          + "&_include=ServiceRequest:requester"
          + "&_include=PractitionerRole:organization"
          + "&_include=PractitionerRole:practitioner"
          + "&_include=ServiceRequest:insurance:Coverage",
      Bundle.class);

  public static final PrefetchTemplateElement SUPPLY_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "supplyRequestBundle",
      "SupplyRequest?_id={{context.orders.SupplyRequest.id}}&"
          + "_include=SupplyRequest:patient"
          + "&_include=SupplyRequest:supplier:Organization"
          + "&_include=SupplyRequest:requester:Practitioner"
          + "&_include=SupplyRequest:requester:Organization"
          + "&_include=SupplyRequest:Requester:PractitionerRole"
          + "&_include=PractitionerRole:organization"
          + "&_include=PractitionerRole:practitioner"
          + "&_include=SupplyRequest:insurance:Coverage",
      Bundle.class);

}