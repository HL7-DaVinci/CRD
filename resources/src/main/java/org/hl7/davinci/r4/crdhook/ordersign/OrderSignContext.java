package org.hl7.davinci.r4.crdhook.ordersign;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hl7.davinci.r4.JacksonBundleDeserializer;
import org.hl7.davinci.r4.JacksonHapiSerializer;
import org.hl7.davinci.r4.crdhook.ServiceContext;
import org.hl7.fhir.r4.model.*;

import javax.validation.constraints.NotNull;

public class OrderSignContext extends ServiceContext {

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
  private Bundle draftOrders;

  public String getUserId() { return userId; }

  public void setUserId(String userId) { this.userId = userId; }

  public String getPatientId() { return patientId; }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public String getEncounterId() {
    return encounterId;
  }

  public void setEncounterId(String encounterId) {
    this.encounterId = encounterId;
  }

  public Bundle getDraftOrders() { return draftOrders; }

  public void setDraftOrders(Bundle draftOrders) {
    this.draftOrders = draftOrders;
  }

}
