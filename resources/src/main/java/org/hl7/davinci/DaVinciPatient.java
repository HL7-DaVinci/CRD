package org.hl7.davinci;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;


@ResourceDef(name = "DaVinciPatient", profile = "http://.../DaVinciPatient")
public class DaVinciPatient extends Patient {


  /**
   * Each extension is defined in a field. Any valid HAPI Data Type
   * can be used for the field type. Note that the [name=""] attribute
   * in the @Child annotation needs to match the name for the bean accessor
   * and mutator methods.
   */
  @Child(name = "favoriteColor")
  @Extension(url = "http://.../blah#favoriteColor", definedLocally = false, isModifier = false)
  @Description(shortDefinition = "The patient's favorite color")
  private StringType favoriteColor;


  /**
   * Gets the favorite color of the patient.
   * @return the string of the favorite color of the patient.
   */
  public StringType getfavoriteColor() {
    if (favoriteColor == null) {
      favoriteColor = new StringType();
    }
    return favoriteColor;
  }

  public void setFavoriteColor(StringType favoriteColor) {
    this.favoriteColor = favoriteColor;
  }


}
