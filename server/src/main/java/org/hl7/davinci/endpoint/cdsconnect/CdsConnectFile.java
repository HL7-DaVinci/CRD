package org.hl7.davinci.endpoint.cdsconnect;

import com.google.gson.JsonElement;



public class CdsConnectFile {

  CdsConnectConnection connection;
  JsonElement jsonFileElement;

  public CdsConnectFile(CdsConnectConnection connection, JsonElement jsonFileElement) {
    this.connection = connection;
    this.jsonFileElement = jsonFileElement;
  }

  public byte[] getCqlBundle() {
    String cqlBundleLocation = jsonFileElement.getAsString();
    return connection.retrieveCqlBundle(cqlBundleLocation);
  }

  public String getFilename() {
    return jsonFileElement.getAsString();
  }
}
