package org.hl7.davinci.stu3.crdhook.ordersign;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.validation.constraints.NotNull;
import org.hl7.davinci.stu3.JacksonBundleDeserializer;
import org.hl7.davinci.stu3.JacksonHapiSerializer;
import org.hl7.davinci.stu3.crdhook.ServiceContext;
import org.hl7.fhir.dstu3.model.Bundle;

public class OrderSignContext extends ServiceContext {

  /** The FHIR Patient.id of the current patient in context. REQUIRED */
  @NotNull
  private String patientId;

  /** The FHIR Encounter.id of the current encounter in context. OPTIONAL */
  private String encounterId;


  /**
   * STU3 - FHIR Bundle of MedicationRequest, ReferralRequest, ProcedureRequest, NutritionOrder,
   * VisionPrescription. REQUIRED
   */
  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle draftOrders;

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

  public Bundle getDraftOrders() {
    return draftOrders;
  }

  public void setDraftOrders(Bundle draftOrders) {
    this.draftOrders = draftOrders;
  }

}
