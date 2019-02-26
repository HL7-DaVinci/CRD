//package org.hl7.davinci.endpoint.database;
//
//import java.io.File;
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.List;
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Table;
//import javax.persistence.Transient;
//import org.hl7.davinci.endpoint.cql.bundle.CqlBundle;
//
//// patientAgeRangeLow, patientAgeRangeHigh,
//// patientGender, patientPlanId, equipmentCode,
//// noAuthNeeded, infoLink
//@Entity
//@Table(name = "coverage_requirement_rules")
//public class CoverageRequirementRuleDatabase {
//  @Id
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
//  @Column(name = "id", updatable = false, nullable = false)
//  private long id;
//
//  //    The following fields describe the rule
//  @Column(name = "payor", nullable = false)
//  private String payor;
//
//  @Column(name = "code", nullable = false)
//  private String code;
//
//  @Column(name = "code_system", nullable = false)
//  private String codeSystem;
//
//
//  public long getId() {
//    return id;
//  }
//
//  public String getPayor() {
//    return payor;
//  }
//
//  public String getCode() {
//    return code;
//  }
//
//  public String getCodeSystem() {
//    return codeSystem;
//  }
//
//  public void setId(long id) {
//    this.id = id;
//  }
//
//  public void setPayor(String payor) {
//    this.payor = payor;
//  }
//
//  public void setCode(String code) {
//    this.code = code;
//  }
//
//  public void setCodeSystem(String codeSystem) {
//    this.codeSystem = codeSystem;
//  }
//
//  @Column(name = "cql_package_path", nullable = false, length = 4000)
//  private String cqlPackagePath;
//
//  public String getCqlPackagePath() {
//    return cqlPackagePath;
//  }
//
//  public void setCqlPackagePath(String cqlPackagePath) {
//    this.cqlPackagePath = cqlPackagePath;
//  }
//
//  @Transient
//  private CqlBundle cqlBundle = null;
//
//  public CqlBundle getCqlBundle() {
//    if (cqlBundle == null){
//      cqlBundle = CqlBundle.fromZip(new File(getCqlPackagePath()));
//    }
//    return cqlBundle;
//  }
//
//  public void setCqlBundle(CqlBundle cqlBundle) {
//    this.cqlBundle = cqlBundle;
//  }
//
//  @Override
//  public String toString() {
//    return String.format("(row id: %d) Payor: %s, Code: %s, CodeSystem: %s", id, payor, code, codeSystem);
//  }
//
//  public CoverageRequirementRuleDatabase() {}
//
//  /**
//   * Returns the name of the fields for dynamic generation of html files.
//   *
//   * @return the list of strings of all the member variables of this class
//   */
//  public static List<String> getFields() {
//    List<String> fieldList = new ArrayList<>();
//    for (Field field : CoverageRequirementRuleDatabase.class.getDeclaredFields()) {
//      String name = field.getName();
//      fieldList.add(name);
//    }
//    return fieldList;
//  }
//}
