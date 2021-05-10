package org.hl7.davinci.endpoint.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "clients")
public class Client {
  @Id
  @Column(name = "client_id", nullable = false, length = 100)
  private String client_id;

  @Column(name = "iss", nullable = false, length = 1000)
  private String iss;

  public Client() {}

  public String getClient_id() {
    return this.client_id;
  }

  public void setClient_id(String id) {
    this.client_id = id;
  }

  public String getIss() {
    return this.iss;
  }

  public void setIss(String iss) {
    this.iss = iss;
  }
}
