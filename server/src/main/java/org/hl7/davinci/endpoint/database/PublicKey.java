package org.hl7.davinci.endpoint.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "public_keys")
public class PublicKey {
  @Id
  @Column(name = "id", nullable = false)
  private String id;

  @Column(name = "key", nullable = false, length = 100000)
  private String key;

  public PublicKey() {}

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }

}
