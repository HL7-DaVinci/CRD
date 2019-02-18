package org.hl7.davinci.endpoint.cdsconnect;

import com.google.gson.*;



public class CdsConnectFile {

  CdsConnectConnection connection;
  JsonElement jsonFileElement;

  public CdsConnectFile(CdsConnectConnection connection, JsonElement jsonFileElement) {
    this.connection = connection;
    this.jsonFileElement = jsonFileElement;
  }

  public String getCql() {
    String cqlFileLocation = jsonFileElement.getAsString();
    return connection.retrieveCqlFile(cqlFileLocation);
  }
}
