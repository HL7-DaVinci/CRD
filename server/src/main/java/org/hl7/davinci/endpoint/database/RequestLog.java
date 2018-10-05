package org.hl7.davinci.endpoint.database;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;


// request_body: BLOB
// timestamp: timestamp
// patient_age: integer
// patient_gender: string
// code: string
// code_system: string
// hook_type: string
// fhir_version: string
// rule_found: string
// results: string (“rule found is: x”, “no record found”, “error: xyz”)

@Entity
@Table(name = "request_log")
public class RequestLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false, nullable = false)
  private long id;

  @Column(name = "request_body", length=10000, nullable = false)
  private byte[] requestBody;

  @Column(name = "timestamp", nullable = false)
  private long timestamp;

  @Column(name = "patient_age")
  private int patientAge;

  @Column(name = "patient_gender")
  private String patientGender;

  @Column(name = "code")
  private String code;

  @Column(name = "code_system")
  private String codeSystem;

  @Column(name = "hook_type")
  private String hookType;

  @Column(name = "fhir_version")
  private String fhirVersion;

  @Column(name = "rule_found")
  private String ruleFound;

  @Column(name = "results")
  private String results;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public byte[] getRequestBody() { return requestBody; }

  public void setRequestBody(byte[] requestBody) { this.requestBody = requestBody; }

  public long getTimestamp() { return this.timestamp; }

  public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

  public int getPatientAge() { return this.patientAge; }

  public void setPatientAge(int patientAge) { this.patientAge = patientAge; }

  public String getPatientGender() { return this.patientGender; }

  public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

  public String getCode() { return this.code; }

  public void setCode(String code) { this.code = code; }

  public String getCodeSystem() { return this.codeSystem; }

  public void setCodeSystem(String codeSystem) { this.codeSystem = codeSystem; }

  public String getHookType() { return this.hookType; }

  public void setHookType(String hookType) { this.hookType = hookType; }

  public String getFhirVersion() { return this.fhirVersion; }

  public void setFhirVersion(String fhirVersion) { this.fhirVersion = fhirVersion; }

  public String getRuleFound() { return this.ruleFound; }

  public void setRuleFound(String ruleFound) { this.ruleFound = ruleFound; }

  public String getResults() { return this.results; }

  public void setResults(String results) { this.results = results; }


  @Override
  public String toString() {
    return String.format("(row id: %d, ts: %d, age: %d, gender: %s, code: %s, system: %s, "
            + "type: %s, version: %s, rule: %s, results %s) Request ",
        id, timestamp, patientAge, patientGender, code, codeSystem,
        hookType, fhirVersion, ruleFound, results);
  }

  public RequestLog() {}

  public RequestLog(byte[] requestBody, long timestamp) {
    setRequestBody(requestBody);
    setTimestamp(timestamp);
  }

  /**
   * Returns the name of the fields for dynamic generation of html files.
   *
   * @return the list of strings of all the member variables of this class
   */
  public static List<String> getFields() {
    List<String> fieldList = new ArrayList<>();
    for (Field field : RequestLog.class.getDeclaredFields()) {
      String name = field.getName();
      fieldList.add(name);
    }
    return fieldList;
  }
}
