package org.hl7.davinci.cdshooks;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.HashMap;
import org.hl7.davinci.JacksonBundleDeserializer;
import org.hl7.davinci.JacksonHapiSerializer;
import org.hl7.fhir.r4.model.*;

/**
 * Class that supports the representation of prefetch information in a CDS Hook request.
 * It appears that for CRD, prefetch information will be the same, regardless of hook type (order-review or
 * medication-prescribe).
 */
public class CrdPrefetch {

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle deviceRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle medicationRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle nutritionOrderBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle serviceRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle supplyRequestBundle;

  public static final String deviceRequestBundleKey = "deviceRequestBundle";
  public static final String deviceRequestBundleQuery =
      "DeviceRequest?id={{context.orders.DeviceRequest.id}}"
          + "&_include=DeviceRequest:patient"
          + "&_include=DeviceRequest:performer"
          + "&_include=DeviceRequest:requester"
          + "&_include=DeviceRequest:device"
          + "&_include=PractitionerRole:organization"
          + "&_include=PractitionerRole:practitioner"
          + "&_include=DeviceRequest:insurance:Coverage";

  public static final String medicationRequestBundleKey = "medicationRequestBundle";
  public static final String medicationRequestBundleQuery =
      "MedicationRequest?id={{context.orders.MedicationRequest.id}}"
        + "&_include=MedicationRequest:patient"
        + "&_include=MedicationRequest:intended-dispenser"
        + "&_include=MedicationRequest:requester:PractitionerRole"
        + "&_include=MedicationRequest:medication"
        + "&_include=PractitionerRole:organization"
        + "&_include=PractitionerRole:practitioner"
        + "&_include=MedicationRequest:insurance:Coverage";

  public static final String nutritionOrderBundleKey = "nutritionOrderBundle";
  public static final String nutritionOrderBundleQuery =
      "NutritionOrder?id={{context.orders.NutritionOrder.id}}"
        + "&_include=NutritionOrder:patient"
        + "&_include=NutritionOrder:provider"
        + "&_include=NutritionOrder:requester"
        + "&_include=PractitionerRole:organization"
        + "&_include=PractitionerRole:practitioner"
        + "&_include=NutritionOrder:encounter"
        + "&_include=Encounter:location"
        + "&_include=NutritionOrder:insurance:Coverage";

  public static final String serviceRequestBundleKey = "serviceRequestBundle";
  public static final String serviceRequestBundleQuery =
      "ServiceRequest?id={{context.orders.ServiceRequest.id}}"
        + "&_include=ServiceRequest:patient"
        + "&_include=ServiceRequest:performer"
        + "&_include=ServiceRequest:requester"
        + "&_include=PractitionerRole:organization"
        + "&_include=PractitionerRole:practitioner"
        + "&_include=ServiceRequest:insurance:Coverage";

  public static final String supplyRequestBundleKey = "supplyRequestBundle";
  public static final String supplyRequestBundleQuery =
      "SupplyRequest?id={{context.orders.SupplyRequest.id}}&"
        + "_include=SupplyRequest:patient"
        + "&_include=SupplyRequest:supplier:Organization"
        + "&_include=SupplyRequest:requester:Practitioner"
        + "&_include=SupplyRequest:requester:Organization"
        + "&_include=SupplyRequest:Requester:PractitionerRole"
        + "&_include=PractitionerRole:organization"
        + "&_include=PractitionerRole:practitioner"
        + "&_include=SupplyRequest:insurance:Coverage";

  public Bundle getDeviceRequestBundle() {
    return deviceRequestBundle;
  }

  public void setDeviceRequestBundle(Bundle deviceRequestBundle) {
    this.deviceRequestBundle = deviceRequestBundle;
  }

  public Bundle getMedicationRequestBundle() {
    return medicationRequestBundle;
  }

  public void setMedicationRequestBundle(Bundle medicationRequestBundle) {
    this.medicationRequestBundle = medicationRequestBundle;
  }

  public Bundle getNutritionOrderBundle() {
    return nutritionOrderBundle;
  }

  public void setNutritionOrderBundle(Bundle nutritionOrderBundle) {
    this.nutritionOrderBundle = nutritionOrderBundle;
  }

  public Bundle getServiceRequestBundle() {
    return serviceRequestBundle;
  }

  public void setServiceRequestBundle(Bundle serviceRequestBundle) {
    this.serviceRequestBundle = serviceRequestBundle;
  }

  public Bundle getSupplyRequestBundle() {
    return supplyRequestBundle;
  }

  public void setSupplyRequestBundle(Bundle supplyRequestBundle) {
    this.supplyRequestBundle = supplyRequestBundle;
  }
}
