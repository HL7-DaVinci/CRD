package org.hl7.davinci.stu3.crdhook.medicationprescribe;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.validation.constraints.NotNull;
import org.hl7.davinci.stu3.JacksonBundleDeserializer;
import org.hl7.davinci.stu3.JacksonHapiSerializer;
import org.hl7.fhir.dstu3.model.Bundle;

public class MedicationPrescribeContext {
  /** The FHIR Patient.id of the current patient in context. REQUIRED */
  @NotNull
  private String patientId;

  /** The FHIR Encounter.id of the current encounter in context. OPTIONAL */
  private String encounterId;

  /**
   * STU3 - FHIR Bundle of MedicationRequest resources. REQUIRED
   */
  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle medications;

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

  public Bundle getMedications() {
    return medications;
  }

  public void setMedications(Bundle medications) {
    this.medications = medications;
  }
}
