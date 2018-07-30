package endpoint.database;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;


//patientAgeRangeLow, patientAgeRangeHigh,
// patientGender, patientPlanId, equipmentCode,
// noAuthNeeded, infoLink
@Entity
@Table(name = "coverage_requirement_rules")
public class DMECoverageRequirementRule {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", updatable = false, nullable = false)
  private long id;

  //    The following fields describe the rule
  @Column(name = "equipment_code", nullable = false)
  private String equipmentCode;

  @Column(name="age_range_low", nullable = false)
  private int ageRangeLow;

  @Column(name="age_range_high", nullable = false)
  private int ageRangeHigh;

  @Column(name="gender_code", nullable = true)
  private Character genderCode;

  //    The following fields describe the rule outcome
  @Column(name="info_link", nullable = true, length = 2000)
  private String infoLink;

  @Column(name="no_auth_needed", nullable = false)
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

  public void setInfoLink(String info_link) {
    this.infoLink = info_link;
  }

  public boolean getNoAuthNeeded() {
    return noAuthNeeded;
  }

  public void setNoAuthNeeded(boolean no_auth_needed) {
    this.noAuthNeeded = no_auth_needed;
  }

  public int getAgeRangeLow() {
    return ageRangeLow;
  }

  public void setAgeRangeLow(int age_range_low) {
    this.ageRangeLow = age_range_low;
  }

  public int getAgeRangeHigh() {
    return ageRangeHigh;
  }

  public void setAgeRangeHigh(int age_range_high) {
    this.ageRangeHigh = age_range_high;
  }

  public Character getGenderCode() {
    return genderCode;
  }

  public void setGenderCode(Character gender_code) {
    this.genderCode = gender_code;
  }

  public String getEquipmentCode() {
    return equipmentCode;
  }

  public void setEquipmentCode(String equipment_code) {
    this.equipmentCode = equipment_code;
  }


  @Override
  public String toString() {
    return "(row id:"+id+") " +
            "  Rule [equipment_code: "+ equipmentCode +", age_range_low: " + ageRangeLow + ", age_range_high: "+ ageRangeHigh +", gender_code: "+ genderCode +"] " +
            "  Outcome [no_auth_needed: "+noAuthNeeded+", info_link: '"+infoLink+"']";
  }





  public DMECoverageRequirementRule() {

  }


  /**
   *
   * @param equipmentCode
   * @param ageRangeLow
   * @param ageRangeHigh
   * @param genderCode
   * @param noAuthNeeded
   * @param infoLink
   */
  public DMECoverageRequirementRule(String equipmentCode, int ageRangeLow, int ageRangeHigh, Character genderCode, Boolean noAuthNeeded, String infoLink) {
    this.equipmentCode = equipmentCode;
    this.ageRangeLow = ageRangeLow;
    this.ageRangeHigh = ageRangeHigh;
    this.genderCode = genderCode;
    this.noAuthNeeded = noAuthNeeded;
    this.infoLink = infoLink;
  }

  /**
   * Returns the name of the fields for dynamic generation of html files.
   * @return the list of strings of all the member variables of this class
   */
  public static List<String> getFields() {
    List<String> fieldList = new ArrayList<>();
    for (Field field : DMECoverageRequirementRule.class.getDeclaredFields()) {
      String name = field.getName();
      fieldList.add(name);
    }
    return fieldList;
  }


}
