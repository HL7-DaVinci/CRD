package org.hl7.davinci.endpoint.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Type;
import com.fasterxml.jackson.databind.JsonNode;


@Entity
@Table(name = "rems")
@TypeDef(name = "json", typeClass = JsonType.class, defaultForType = JsonNode.class )
public class Rems {
  @Id
  @Column(name = "case_number", nullable = false, length = 100)
  private String case_number;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "complianceBundle", nullable = false, length = 10000000)
  private JsonNode complianceBundle;

  @Column(name = "status", nullable = false, length = 100)
  private String status;

  public void Rems() {}

  public String getCase_number() {
    return this.case_number;
  }

  public void setCase_number(String id) {
    this.case_number = id;
  }

  public JsonNode getComplianceBundle() {
    return this.complianceBundle;
  }

  public void setComplianceBundle(JsonNode jsonParam) {
    this.complianceBundle = jsonParam;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(String statusParam) {
    this.status = statusParam;
  }
}
