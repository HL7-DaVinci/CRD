package org.hl7.davinci.stu3.crdhook;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hl7.davinci.stu3.JacksonBundleDeserializer;
import org.hl7.davinci.stu3.JacksonHapiSerializer;
import org.hl7.fhir.dstu3.model.Bundle;

/**
 * Class that supports the representation of prefetch information in a CDS Hook request.
 * It appears that for CRD, prefetch information will be the same, regardless of hook type (order-review,
 * medication-prescribe, or order-select).
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
  private Bundle procedureRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle referralRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle supplyRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle visionPrescriptionBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle appointmentBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle encounterBundle;

  public Bundle getDeviceRequestBundle() {
    return deviceRequestBundle;
  }

  public void setDeviceRequestBundle(Bundle deviceRequestBundle) {
    this.deviceRequestBundle = deviceRequestBundle;
  }

  public Bundle getMedicationRequestBundle() {
    return medicationRequestBundle;
  }

  public void setMedicationRequestBundle(Bundle medicationRequestBundle) { this.medicationRequestBundle = medicationRequestBundle; }

  public Bundle getNutritionOrderBundle() {
    return nutritionOrderBundle;
  }

  public void setNutritionOrderBundle(Bundle nutritionOrderBundle) {
    this.nutritionOrderBundle = nutritionOrderBundle;
  }

  public Bundle getProcedureRequestBundle() {
    return procedureRequestBundle;
  }

  public void setProcedureRequestBundle(Bundle procedureRequestBundle) { this.procedureRequestBundle = procedureRequestBundle; }

  public Bundle getReferralRequestBundle() {
    return referralRequestBundle;
  }

  public void setReferralRequestBundle(Bundle referralRequestBundle) { this.referralRequestBundle = referralRequestBundle; }

  public Bundle getSupplyRequestBundle() {
    return supplyRequestBundle;
  }

  public void setSupplyRequestBundle(Bundle supplyRequestBundle) {
    this.supplyRequestBundle = supplyRequestBundle;
  }

  public Bundle getVisionPrescriptionBundle() { return visionPrescriptionBundle; }

  public void setVisionPrescriptionBundle(Bundle visionPrescriptionBundle) { this.visionPrescriptionBundle = visionPrescriptionBundle; }

  public Bundle getAppointmentBundle() { return appointmentBundle; }

  public void setAppointmentBundle(Bundle appointmentBundle) { this.appointmentBundle = appointmentBundle; }

  public Bundle getEncounterBundle() { return encounterBundle; }

  public void setEncounterBundle(Bundle encounterBundle) { this.encounterBundle = encounterBundle; }
}
