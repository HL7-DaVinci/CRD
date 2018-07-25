package fhir.restful.database;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;





//patientAgeRangeLow, patientAgeRangeHigh,
// patientGender, patientPlanId, equipmentCode,
// noAuthNeeded, infoLink
@Entity
public class Datum {

  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = false)
  private String patientAgeRangeLow;

  @Column(nullable = false)
  private String patientAgeRangeHigh;

  @Column(nullable = false)
  private String patientGender;

  @Column(nullable = false)
  private String patientPlanId;

  @Column(nullable = false)
  private String equipmentCode;

  @Column(nullable = false)
  private String noAuthNeeded;

  @Column(nullable = false)
  private String infoLink;

  public Datum() {

  }

  /**
   * Constructor for the base data that will be put in the repository.
   * @param patientAgeRangeLow lower bound of the patient age
   * @param patientAgeRangeHigh upper bound of the patient age
   * @param patientGender gender of the patient
   * @param patientPlanId the ID of the patient's care plan
   * @param equipmentCode the code of the equipment being asked for
   * @param noAuthNeeded whether prior authorization is needed
   * @param infoLink a link to some resource relevant to the request
   */
  public Datum(String patientAgeRangeLow, String patientAgeRangeHigh, String patientGender,
               String patientPlanId, String equipmentCode, String noAuthNeeded, String infoLink) {
    this.patientAgeRangeLow = patientAgeRangeLow;
    this.patientAgeRangeHigh = patientAgeRangeHigh;
    this.patientGender = patientGender;
    this.patientPlanId = patientPlanId;
    this.equipmentCode = equipmentCode;
    this.noAuthNeeded = noAuthNeeded;
    this.infoLink = infoLink;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPatientAgeRangeLow() {
    return patientAgeRangeLow;
  }

  public void setPatientAgeRangeLow(String patientAgeRangeLow) {
    this.patientAgeRangeLow = patientAgeRangeLow;
  }

  public String getPatientAgeRangeHigh() {
    return patientAgeRangeHigh;
  }

  public void setPatientAgeRangeHigh(String patientAgeRangeHigh) {
    this.patientAgeRangeHigh = patientAgeRangeHigh;
  }

  public String getPatientGender() {
    return patientGender;
  }

  public void setPatientGender(String patientGender) {
    this.patientGender = patientGender;
  }

  public String getPatientPlanId() {
    return patientPlanId;
  }

  public void setPatientPlanId(String patientPlanId) {
    this.patientPlanId = patientPlanId;
  }

  public String getEquipmentCode() {
    return equipmentCode;
  }

  public void setEquipmentCode(String equipmentCode) {
    this.equipmentCode = equipmentCode;
  }

  public String getNoAuthNeeded() {
    return noAuthNeeded;
  }

  public void setNoAuthNeeded(String noAuthNeeded) {
    this.noAuthNeeded = noAuthNeeded;
  }

  public String getInfoLink() {
    return infoLink;
  }

  public void setInfoLink(String infoLink) {
    this.infoLink = infoLink;
  }


  @Override
  public String toString() {
    return this.equipmentCode;
  }

  /**
   * Returns the name of the fields for dynamic generation of html files.
   * @return the list of strings of all the member variables of this class
   */
  public static List<String> getFields() {
    List<String> fieldList = new ArrayList<>();
    for (Field field : Datum.class.getDeclaredFields()) {
      String name = field.getName();
      fieldList.add(name);
    }
    return fieldList;
  }


}
