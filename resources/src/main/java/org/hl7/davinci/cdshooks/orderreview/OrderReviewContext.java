package org.hl7.davinci.cdshooks.orderreview;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hl7.davinci.JacksonBundleDeserializer;
import org.hl7.davinci.JacksonHapiSerializer;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.*;

import javax.validation.constraints.NotNull;

public class OrderReviewContext {

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
  private Bundle orders;

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

  public Bundle getOrders() {
    return orders;
  }

  public void setOrders(Bundle orders) {
    this.orders = orders;
  }

  public CodeableConcept firstOrderCode() throws FHIRException {
    Resource r = this.getOrders().getEntry().get(0).getResource();
    if (r.getResourceType() == ResourceType.DeviceRequest) {
      DeviceRequest dr = (DeviceRequest) r;
      return dr.getCodeCodeableConcept();
    } else {
      return null;
    }
  }
}
