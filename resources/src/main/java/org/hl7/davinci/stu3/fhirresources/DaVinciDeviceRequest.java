package org.hl7.davinci.stu3.fhirresources;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.dstu3.model.DeviceRequest;
import org.hl7.fhir.dstu3.model.Reference;

@ResourceDef(name = "DeviceRequest", profile =
    "http://hl7.org/fhir/us/davinci-crd/STU3/StructureDefinition/profile-devicerequest-stu3")
public class DaVinciDeviceRequest extends DeviceRequest {

  @Child(name = "insurance")
  @Extension(url = "http://build.fhir.org/ig/HL7/davinci-crd/STU3/ext-insurance.html",
      definedLocally = false, isModifier = false)
  @Description(shortDefinition = "Associated insurance coverage")
  private List<Reference> insurance;

  /**
   * Gets the insurance.
   *
   * @return the insurance
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
   * Adds a new insurance.
   *
   * @param insurance the reference to the insurance to be added
   */
  public void addInsurance(Reference insurance) {
    if (this.insurance == null) {
      this.insurance = new ArrayList<>();
    }
    this.insurance.add(insurance);
  }

}