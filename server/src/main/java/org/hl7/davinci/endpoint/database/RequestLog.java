package org.hl7.davinci.endpoint.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// request_body: BLOB
// timestamp: timestamp
// patient_age: integer
// patient_gender: string
// code: string
// code_system: string
// hook_type: string
// fhir_version: string
// rules_found: Set<CoverageRequirementRule>
// timeline: boolean[]

@Entity
@Table(name = "request_log")
public class RequestLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false, nullable = false)
  private long id;

  @Column(name = "request_body", length = 10000, nullable = false)
  private byte[] requestBody;

  @Column(name = "timestamp", nullable = false)
  private long timestamp;

  @Column(name = "patient_age")
  private int patientAge;

  @Column(name = "patient_gender")
  private String patientGender;

  @Column(name = "patient_address_state")
  private String patientAddressState;

  @Column(name = "provider_address_state")
  private String providerAddressState;

  @Column(name = "code")
  private String code;

  @Column(name = "code_system")
  private String codeSystem;

  @Column(name = "hook_type")
  private String hookType;

  @Column(name = "fhir_version")
  private String fhirVersion;

  @Column(name = "results")
  private String results;

  @Column(name = "timeline")
  private boolean[] timeline;

  private int timelineCounter;

  @ManyToMany(cascade = {
      CascadeType.PERSIST,
      CascadeType.MERGE
  })
  @JoinTable(name = "request_rule",
      joinColumns = @JoinColumn(name = "reququest_id"),
      inverseJoinColumns = @JoinColumn(name = "rule_id")
  )
  private Set<CoverageRequirementRule> rulesFound = new HashSet<>();

  public RequestLog() {
  }

  public RequestLog(byte[] requestBody, long timestamp) {
    // needed constructor for auth failure logging.
    setRequestBody(requestBody);
    setTimestamp(timestamp);
  }
  public RequestLog(Object request, long timestamp, String fhirVersion,
                    String hookType, RequestService requestService, int sections) {
    setTimestamp(timestamp);
    setFhirVersion(fhirVersion);
    setHookType(hookType);
    boolean[] timeline = new boolean[sections];
    // one for free because if we're here, we're authorized
    timeline[0] = true;
    setTimeline(timeline);
    this.timelineCounter = 1;
    String requestStr;
    try {
      ObjectMapper mapper = new ObjectMapper();
      ObjectWriter w = mapper.writer();
      requestStr = w.writeValueAsString(request);
    } catch (Exception e) {
//      logger.error("failed to write request json: " + e.getMessage());
      requestStr = "error";
    }

    setRequestBody(requestStr.getBytes());

    requestService.create(this);
  }

  public void advanceTimeline(RequestService requestService) {
    this.timeline[this.timelineCounter] = true;
    this.timelineCounter++;
    requestService.edit(this);

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

  public void gatherInfo(List<CoverageRequirementRuleQuery>  queries, List<String> codes, List<String> codeSystems) {
//    this.setPatientAge(queries.get(0).getCriteria().getAge());
//    this.setPatientGender(String.valueOf(queries.get(0).getCriteria().getGenderCode()));
//    this.setPatientAddressState(queries.get(0).getCriteria().getPatientAddressState());
//    this.setProviderAddressState(queries.get(0).getCriteria().getProviderAddressState());
    this.setCode(String.join(", ", codes));

    this.setCodeSystem(String.join(", ", codeSystems));
  }
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public byte[] getRequestBody() {
    return requestBody;
  }

  public void setRequestBody(byte[] requestBody) {
    this.requestBody = requestBody;
  }

  public long getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public int getPatientAge() {
    return this.patientAge;
  }

  public void setPatientAge(int patientAge) {
    this.patientAge = patientAge;
  }

  public String getPatientGender() {
    return this.patientGender;
  }

  public void setPatientGender(String patientGender) {
    this.patientGender = patientGender;
  }

  public String getPatientAddressState() {
    return patientAddressState;
  }

  public void setPatientAddressState(String patientAddressState) {
    this.patientAddressState = patientAddressState;
  }

  public String getProviderAddressState() {
    return providerAddressState;
  }

  public void setProviderAddressState(String providerAddressState) {
    this.providerAddressState = providerAddressState;
  }

  public String getCode() {
    return this.code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getCodeSystem() {
    return this.codeSystem;
  }

  public void setCodeSystem(String codeSystem) {
    this.codeSystem = codeSystem;
  }

  public String getHookType() {
    return this.hookType;
  }

  public void setHookType(String hookType) {
    this.hookType = hookType;
  }

  public String getFhirVersion() {
    return this.fhirVersion;
  }

  public void setFhirVersion(String fhirVersion) {
    this.fhirVersion = fhirVersion;
  }

  public String getResults() {
    return this.results;
  }

  public void setResults(String results) {
    this.results = results;
  }

  public boolean[] getTimeline() {
    return this.timeline;
  }

  public void setTimeline(boolean[] timeline) {
    this.timeline = timeline;
  }

  public Set<CoverageRequirementRule> getRulesFound() {
    return this.rulesFound;
  }

  public void addRuleFound(CoverageRequirementRule coverageRequirementRule) {
    this.rulesFound.add(coverageRequirementRule);
  }

  public void addRulesFound(List<CoverageRequirementRule> rules) {
    this.rulesFound.addAll(rules);
  }

  @Override
  public String toString() {
    return String.format(
        "(row id: %d, ts: %d, age: %d, gender: %s, code: %s, system: %s, patient state: %s, "
            + "provider state: %s, type: %s, version: %s results %s) Request ",
        id, timestamp, patientAge, patientGender, code, codeSystem, patientAddressState,
        providerAddressState, hookType, fhirVersion, results);
  }
}
