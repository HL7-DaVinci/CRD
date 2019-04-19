package org.hl7.davinci.ehrserver.requestgenerator.database;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "publickeys")
public class Key {
  private String id;
  private String kty;
  private String n;
  private String e;

  public Key() {
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getN() {

    return n;
  }

  public void setN(String n) {
    this.n = n;
  }

  public String getE() {

    return e;
  }

  public String getKty() {
    return kty;
  }

  public void setKty(String kty) {
    this.kty = kty;
  }

  public void setE(String e) {
    this.e = e;

  }





}
