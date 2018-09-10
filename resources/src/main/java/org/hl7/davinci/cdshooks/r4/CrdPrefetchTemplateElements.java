package org.hl7.davinci.cdshooks.r4;

/**
 * Class that contains the different prefetch template elements used in crd requests.
 */
public class CrdPrefetchTemplateElements {

  public static final PrefetchTemplateElement DEVICE_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "deviceRequestBundle",
      "DeviceRequest?id={{context.orders.DeviceRequest.id}}"
          + "&_include=DeviceRequest:patient"
          + "&_include=DeviceRequest:performer"
          + "&_include=DeviceRequest:requester"
          + "&_include=DeviceRequest:device"
          + "&_include=PractitionerRole:organization"
          + "&_include=PractitionerRole:practitioner"
          + "&_include=DeviceRequest:insurance:Coverage");
  public static final PrefetchTemplateElement MEDICATION_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "medicationRequestBundle",
      "MedicationRequest?id={{context.orders.MedicationRequest.id}}"
          + "&_include=MedicationRequest:patient"
          + "&_include=MedicationRequest:intended-dispenser"
          + "&_include=MedicationRequest:requester:PractitionerRole"
          + "&_include=MedicationRequest:medication"
          + "&_include=PractitionerRole:organization"
          + "&_include=PractitionerRole:practitioner"
          + "&_include=MedicationRequest:insurance:Coverage");
  public static final PrefetchTemplateElement NUTRITION_ORDER_BUNDLE = new PrefetchTemplateElement(
      "nutritionOrderBundle",
      "NutritionOrder?id={{context.orders.NutritionOrder.id}}"
          + "&_include=NutritionOrder:patient"
          + "&_include=NutritionOrder:provider"
          + "&_include=NutritionOrder:requester"
          + "&_include=PractitionerRole:organization"
          + "&_include=PractitionerRole:practitioner"
          + "&_include=NutritionOrder:encounter"
          + "&_include=Encounter:location"
          + "&_include=NutritionOrder:insurance:Coverage");
  public static final PrefetchTemplateElement SERVICE_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "serviceRequestBundle",
      "ServiceRequest?id={{context.orders.ServiceRequest.id}}"
          + "&_include=ServiceRequest:patient"
          + "&_include=ServiceRequest:performer"
          + "&_include=ServiceRequest:requester"
          + "&_include=PractitionerRole:organization"
          + "&_include=PractitionerRole:practitioner"
          + "&_include=ServiceRequest:insurance:Coverage");
  public static final PrefetchTemplateElement SUPPLY_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "supplyRequestBundle",
      "SupplyRequest?id={{context.orders.SupplyRequest.id}}&"
          + "_include=SupplyRequest:patient"
          + "&_include=SupplyRequest:supplier:Organization"
          + "&_include=SupplyRequest:requester:Practitioner"
          + "&_include=SupplyRequest:requester:Organization"
          + "&_include=SupplyRequest:Requester:PractitionerRole"
          + "&_include=PractitionerRole:organization"
          + "&_include=PractitionerRole:practitioner"
          + "&_include=SupplyRequest:insurance:Coverage");

  public static class PrefetchTemplateElement {

    private String key;
    private String query;

    PrefetchTemplateElement(String key, String query) {
      this.key = key;
      this.query = query;
    }

    public String getKey() {
      return key;
    }

    public String getQuery() {
      return query;
    }
  }
}
