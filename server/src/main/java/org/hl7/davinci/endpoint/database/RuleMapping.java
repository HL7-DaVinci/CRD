package org.hl7.davinci.endpoint.database;

import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rule_mapping")
public class RuleMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false, nullable = false)
  private long id;

  @Column(name = "payer", nullable = false)
  private String payer;

  @Column(name = "code_system", nullable = false)
  private String codeSystem;

  @Column(name = "code", nullable = false)
  private String code;

  @Column(name = "fhir_version", nullable = false)
  private String fhirVersion;

  @Column(name = "topic", nullable = false)
  private String topic;

  @Column(name = "rule_file", nullable = false)
  private String ruleFile;

  @Column(name = "rule_file_path", nullable = true)
  private String ruleFilePath;

  @Column(name = "node", nullable = true)
  private Integer node;

  private String link = "";

  private String readableTopic = "";

  public long getId() {
    return id;
  }

  public RuleMapping setId(long id) {
    this.id = id;
    return this;
  }

  public String getPayer() {
    return payer;
  }

  public RuleMapping setPayer(String payer) {
    this.payer = payer;
    return this;
  }

  public String getCodeSystem() {
    return codeSystem;
  }

  public RuleMapping setCodeSystem(String codeSystem) {
    this.codeSystem = codeSystem;
    return this;
  }

  public String getCode() {
    return code;
  }

  public RuleMapping setCode(String code) {
    this.code = code;
    return this;
  }

  public String getFhirVersion() {
    return fhirVersion;
  }

  public RuleMapping setFhirVersion(String fhirVersion) {
    this.fhirVersion = fhirVersion;
    return this;
  }

  public String getTopic() {
    return topic;
  }

  public RuleMapping setTopic(String topic) {
    this.topic = topic;
    return this;
  }

  public String getRuleFile() {
    return ruleFile;
  }

  public RuleMapping setRuleFile(String ruleFile) {
    this.ruleFile = ruleFile;
    return this;
  }

  public String getRuleFilePath() {
    return ruleFilePath;
  }

  public RuleMapping setRuleFilePath(String ruleFilePath) {
    this.ruleFilePath = ruleFilePath;
    return this;
  }

  public Integer getNode() {
    return node;
  }

  public RuleMapping setNode(Integer node) {
    this.node = node;
    return this;
  }

  @Override
  public String toString() {
    return String.format("(row id: %d) Payer: %s, CodeSystem: %s, Code: %s, FHIR Version: %s, Topic: %s", id, payer, codeSystem, code, fhirVersion, topic);
  }

  public String getLink() {
    if (link.isEmpty()) {
      return "/files/" + topic + "/" + fhirVersion + "/" + ruleFile + "?noconvert=true";
    } else {
      return link;
    }
  }

  public RuleMapping setLink(String link) {
    this.link = link;
    return this;
  }

  public String getReadableTopic() {
    if (readableTopic.isEmpty()) {
      // add a space between the pieces of the CamelCase topic (Camel Case)
      return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(topic), ' ');
    } else {
      return readableTopic;
    }
  }

  public RuleMapping setReadableTopic(String readableTopic) {
    this.readableTopic = readableTopic;
    return this;
  }

  public RuleMapping() {}

  /**
   * Returns the name of the fields for dynamic generation of html files.
   *
   * @return the list of strings of all the member variables of this class
   */
  public static List<String> getFields() {
    List<String> fieldList = new ArrayList<>();
    for (Field field : RuleMapping.class.getDeclaredFields()) {
      String name = field.getName();
      fieldList.add(name);
    }
    return fieldList;
  }
}
