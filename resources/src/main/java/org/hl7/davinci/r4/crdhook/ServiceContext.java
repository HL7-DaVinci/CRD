package org.hl7.davinci.r4.crdhook;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hl7.davinci.EncounterBasedServiceContext;
import org.hl7.davinci.r4.JacksonBundleDeserializer;
import org.hl7.davinci.r4.JacksonHapiSerializer;
import org.hl7.fhir.r4.model.Bundle;

import javax.validation.constraints.NotNull;

public abstract class ServiceContext implements EncounterBasedServiceContext {

  /** The FHIR Practictioner.id of the current practitioner in context. REQUIRED */
  @NotNull
  private String userId = null;

  /** The FHIR Patient.id of the current patient in context. REQUIRED */
  @NotNull
  private String patientId;

  /** The FHIR Encounter.id of the current encounter in context. OPTIONAL */
  private String encounterId;

  /**
   * R4 - FHIR Bundle of MedicationRequest, ReferralRequest, ProcedureRequest, NutritionOrder,
   * VisionPrescription. REQUIRED
   */
  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle services;

  public String getUserId() { return userId; }

  public void setUserId(String userId) { this.userId = userId; }

  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public String getEncounterId() {
    return encounterId;
  }

  public void setEncounterId(String encounterId) {
    this.encounterId = encounterId;
  }

  public Bundle getServices() {
    return services;
  }

  public void setServices(Bundle services) {
    this.services = services;
  }
  
}
