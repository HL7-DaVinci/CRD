package endpoint.cdshooks.services.crd;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

public class OrderReviewContext {

  /** The FHIR Patient.id of the current patient in context. REQUIRED */
  @NotNull private String patientId;

  /** The FHIR Encounter.id of the current encounter in context. OPTIONAL */
  private String encounterId;

  /**
   * STU3 - FHIR Bundle of MedicationRequest, ReferralRequest, ProcedureRequest, NutritionOrder,
   * VisionPrescription. REQUIRED
   */
  // TODO: figure out how to validate this, return usefull message (maybe in fhir processing)
  private List<String> ordersFhirResourceStringList;

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

  public List<String> getOrdersFhirResourceStringList() {
    return ordersFhirResourceStringList;
  }

  /**
   * Set the parameter by parse the nodes and turning them back into strings for later fhir
   * parsing.
   * @param ordersFhirBundleJsonNode The raw jsonnode, automatically input.
   */
  @JsonSetter("orders")
  public void setOrdersFhirResourceStringList(JsonNode ordersFhirBundleJsonNode) {
    List<String> fhirResourceList = new ArrayList<>();
    for (final JsonNode fhirResourceJsonNode : ordersFhirBundleJsonNode.get("entry")) {
      fhirResourceList.add(fhirResourceJsonNode.toString());
    }

    this.ordersFhirResourceStringList = fhirResourceList;
  }
}
