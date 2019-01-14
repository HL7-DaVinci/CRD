package org.hl7.davinci.stu3.fhirresources;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Reference;

@ResourceDef(name = "MedicationRequest", profile =
    "http://hl7.org/fhir/us/davinci-crd/STU3/StructureDefinition/profile-medicationrequest-stu3")
public class DaVinciMedicationRequest extends MedicationRequest {

  @Child(name = "insurance")
  @Extension(url = "http://build.fhir.org/ig/HL7/davinci-crd/STU3/ext-insurance.html",
      definedLocally = false, isModifier = false)
  @Description(shortDefinition = "Associated insurance coverage")
  private List<Reference> insurance;

  /**
   * Gets the insurance.
   *
   * @return the insurance references
   */
  public List<Reference> getInsurance() {
    if (insurance == null) {
      insurance = new ArrayList<>();
    }
    return insurance;
  }

  public void setInsurance(List<Reference> insurance) {
    this.insurance = insurance;
  }

  /**
   * Adds the insurance to the list of insurance references.
   *
   * @param insurance the insurance reference to add
   */
  public void addInsurance(Reference insurance) {
    if (this.insurance == null) {
      this.insurance = new ArrayList<>();
    }
    this.insurance.add(insurance);
  }

}