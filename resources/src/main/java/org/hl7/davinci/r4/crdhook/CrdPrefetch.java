package org.hl7.davinci.r4.crdhook;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hl7.davinci.r4.JacksonBundleDeserializer;
import org.hl7.davinci.r4.JacksonHapiSerializer;
import org.hl7.fhir.r4.model.*;

/**
 * Class that supports the representation of prefetch information in a CDS Hook request.
 * It appears that for CRD, prefetch information will be the same, regardless of hook type.
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

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle appointmentBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle encounterBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle medicationDispenseBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle medicationStatementBundle;

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

  public Bundle getMedicationDispenseBundle() {
    return medicationDispenseBundle;
  }

  public void setMedicationDispenseBundle(Bundle medicationDispenseBundle) { this.medicationDispenseBundle = medicationDispenseBundle; }

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

  public Bundle getAppointmentBundle() { return appointmentBundle; }

  public void setAppointmentBundle(Bundle appointmentBundle) { this.appointmentBundle = appointmentBundle; }

  public Bundle getEncounterBundle() { return encounterBundle; }

  public void setEncounterBundle(Bundle encounterBundle) { this.encounterBundle = encounterBundle; }

  public Bundle getMedicationStatementBundle() { return medicationStatementBundle; }

  public void setMedicationStatementBundle(Bundle medicationStatementBundle) { this.medicationStatementBundle = medicationStatementBundle; }
}
