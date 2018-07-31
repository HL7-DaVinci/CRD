package endpoint.database;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

// patientAgeRangeLow, patientAgeRangeHigh,
// patientGender, patientPlanId, equipmentCode,
// noAuthNeeded, infoLink
@Entity
@Table(name = "coverage_requirement_rules")
public class CoverageRequirementRule {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  private long id;

  //    The following fields describe the rule
  @Column(name = "equipment_code", nullable = false)
  private String equipmentCode;

  @Column(name = "age_range_low", nullable = false)
  private int ageRangeLow;

  @Column(name = "age_range_high", nullable = false)
  private int ageRangeHigh;

  @Column(name = "gender_code", nullable = true)
  private Character genderCode;

  //    The following fields describe the rule outcome
  @Column(name = "info_link", nullable = true, length = 2000)
  private String infoLink;

  @Column(name = "no_auth_needed", nullable = false)
  private boolean noAuthNeeded;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getInfoLink() {
    return infoLink;
  }

  public void setInfoLink(String infoLink) {
    this.infoLink = infoLink;
  }

  public boolean getNoAuthNeeded() {
    return noAuthNeeded;
  }

  public void setNoAuthNeeded(boolean noAuthNeeded) {
    this.noAuthNeeded = noAuthNeeded;
  }

  public int getAgeRangeLow() {
    return ageRangeLow;
  }

  public void setAgeRangeLow(int ageRangeLow) {
    this.ageRangeLow = ageRangeLow;
  }

  public int getAgeRangeHigh() {
    return ageRangeHigh;
  }

  public void setAgeRangeHigh(int ageRangeHigh) {
    this.ageRangeHigh = ageRangeHigh;
  }

  public Character getGenderCode() {
    return genderCode;
  }

  public void setGenderCode(Character genderCode) {
    this.genderCode = genderCode;
  }

  public String getEquipmentCode() {
    return equipmentCode;
  }

  public void setEquipmentCode(String equipmentCode) {
    this.equipmentCode = equipmentCode;
  }

  @Override
  public String toString() {
    return "(row id:"
        + id
        + ") "
        + "  Rule [equipment_code: "
        + equipmentCode
        + ", ageRangeLow: "
        + ageRangeLow
        + ", ageRangeHigh: "
        + ageRangeHigh
        + ", genderCode: "
        + genderCode
        + "] "
        + "  Outcome [noAuthNeeded: "
        + noAuthNeeded
        + ", infoLink: '"
        + infoLink
        + "']";
  }

  public CoverageRequirementRule() {}


  /**
   * Returns the name of the fields for dynamic generation of html files.
   *
   * @return the list of strings of all the member variables of this class
   */
  public static List<String> getFields() {
    List<String> fieldList = new ArrayList<>();
    for (Field field : CoverageRequirementRule.class.getDeclaredFields()) {
      String name = field.getName();
      fieldList.add(name);
    }
    return fieldList;
  }
}
