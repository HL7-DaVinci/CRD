package org.hl7.davinci.stu3.crdhook;

import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.fhir.dstu3.model.Bundle;

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
          + "&_include=DeviceRequest:on-behalf"
          + "&_include=DeviceRequest:insurance:Coverage",
      Bundle.class);

  public static final PrefetchTemplateElement MEDICATION_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "medicationRequestBundle",
      "MedicationRequest?_id={{context.orders.MedicationRequest.id}}"
          + "&_include=MedicationRequest:patient"
          + "&_include=MedicationRequest:intended-dispenser"
          + "&_include=MedicationRequest:requester:Practitioner"
          + "&_include=MedicationRequest:medication"
          + "&_include=MedicationRequest:on-behalf"
          + "&_include=MedicationRequest:insurance:Coverage",
      Bundle.class);

  public static final PrefetchTemplateElement NUTRITION_ORDER_BUNDLE = new PrefetchTemplateElement(
      "nutritionOrderBundle",
      "NutritionOrder?_id={{context.orders.NutritionOrder.id}}"
          + "&_include=NutritionOrder:patient"
          + "&_include=NutritionOrder:provider"
          + "&_include=NutritionOrder:requester"
          + "&_include=NutritionOrder:encounter"
          + "&_include=Enconuter:location"
          + "&_include=NutritionOrder:insurance:Coverage",
      Bundle.class);

  public static final PrefetchTemplateElement PROCEDURE_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "procedureRequestBundle",
      "ProcedureRequest?_id={{context.orders.ProcedureRequest.id}}"
          + "&_include=ProcedureRequest:patient"
          + "&_include=ProcedureRequest:performer"
          + "&_include=ProcedureRequest:requester"
          + "&_include=ProcedureRequest:on-behalf"
          + "&_include=ProcedureRequest:insurance:Coverage",
      Bundle.class);

  public static final PrefetchTemplateElement REFERRAL_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "referralRequest",
      "ReferralRequest?_id={{context.orders.ReferralRequest.id}}"
          + "&_include=ReferralRequest:patient"
          + "&_include=ReferralRequest:recipient"
          + "&_include=ReferralRequest:requester"
          + "&_include=ReferralRequest:on-behalf"
          + "&_include=ReferralRequest:insurance:Coverage",
      Bundle.class);

  public static final PrefetchTemplateElement SUPPLY_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "supplyRequestBundle",
      "SupplyRequest?_id={{context.orders.SupplyRequest.id}}"
          + "&_include=SupplyRequest:patient"
          + "&_include=SupplyRequest:supplier:Organization"
          + "&_include=SupplyRequest:requester:Practitioner"
          + "&_include=SupplyRequest:requester:Organization"
          + "&_include=SupplyRequest:insurance:Coverage",
      Bundle.class);

}
