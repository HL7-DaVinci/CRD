package org.hl7.davinci.endpoint.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundle;

// patientAgeRangeLow, patientAgeRangeHigh,
// patientGender, patientPlanId, equipmentCode,
// noAuthNeeded, infoLink
@Entity
@Table(name = "coverage_requirement_rules")
public class CoverageRequirementRule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false, nullable = false)
  private long id;

  //    The following fields describe the rule
  @Column(name = "payor", nullable = false)
  private String payor;

  @Column(name = "code", nullable = false)
  private String code;

  @Column(name = "code_system", nullable = false)
  private String codeSystem;

  @Column(name = "cql_package_path", nullable = false, length = 4000)
  private String cqlPackagePath;

  public long getId() {
    return id;
  }

  public CoverageRequirementRule setId(long id) {
    this.id = id;
    return this;
  }

  public String getPayor() {
    return payor;
  }

  public CoverageRequirementRule setPayor(String payor) {
    this.payor = payor;
    return this;
  }

  public String getCode() {
    return code;
  }

  public CoverageRequirementRule setCode(String code) {
    this.code = code;
    return this;
  }

  public String getCodeSystem() {
    return codeSystem;
  }

  public CoverageRequirementRule setCodeSystem(String codeSystem) {
    this.codeSystem = codeSystem;
    return this;
  }

  public String getCqlPackagePath() {
    return cqlPackagePath;
  }

  public CoverageRequirementRule setCqlPackagePath(String cqlPackagePath) {
    this.cqlPackagePath = cqlPackagePath;
    return this;
  }

  @Transient
  private CqlBundle cqlBundle = null;

  public void setCqlBundle(CqlBundle cqlBundle) {
    this.cqlBundle = cqlBundle;
  }

  public CqlBundle getCqlBundle() {
    if (cqlBundle == null){
      cqlBundle = CqlBundle.fromZip(new File(getCqlPackagePath()));
    }
    return cqlBundle;
  }

  @Override
  public String toString() {
    return String.format("(row id: %d) Payor: %s, Code: %s, CodeSystem: %s, CqlPackagePath: %s", id, payor, code, codeSystem, cqlPackagePath);
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
