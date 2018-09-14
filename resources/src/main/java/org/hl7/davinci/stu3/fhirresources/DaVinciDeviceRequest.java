package org.hl7.davinci.stu3.fhirresources;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.dstu3.model.DeviceRequest;
import org.hl7.fhir.dstu3.model.Reference;

@ResourceDef(name = "DeviceRequest", profile = "http://base.url/DaVinciDeviceRequest")
public class DaVinciDeviceRequest extends DeviceRequest {

  @Child(name = "insurance")
  @Extension(url = "http://base.url/DaVinciDeviceRequest#insurance", definedLocally = false, isModifier = false)
  @Description(shortDefinition = "Associated insurance coverage")
  private List<Reference> insurance;

  public List<Reference> getInsurance() {
    if (insurance == null) {
      insurance = new ArrayList<>();
    }
    return insurance;
  }

  public void setInsurance(List<Reference> insurance) {
    this.insurance = insurance;
  }

  public void addInsurance(Reference insurance) {
    if (this.insurance == null) {
      this.insurance = new ArrayList<>();
    }
    this.insurance.add(insurance);
  }

}