package org.hl7.davinci.endpoint.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import com.google.gson.JsonObject;


@Entity
@Table(name = "rems")
public class Rems {
  @Id
  @Column(name = "case_number", nullable = false, length = 100)
  private String case_number;

  @Column(name = "json", nullable = false, length = 100000)
  private JsonObject json;

  @Column(name = "status", nullable = false, length = 100)
  private String status;

  public void Rems() {}

  public String getCase_number() {
    return this.case_number;
  }

  public void setCase_number(String id) {
    this.case_number = id;
  }

  public JsonObject getJSON() {
    return this.json;
  }

  public void setJSON(JsonObject jsonParam) {
    this.json = jsonParam;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(String statusParam) {
    this.status = statusParam;
  }
}
