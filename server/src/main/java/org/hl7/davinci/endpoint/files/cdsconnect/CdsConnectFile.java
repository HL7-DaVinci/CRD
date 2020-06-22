package org.hl7.davinci.endpoint.files.cdsconnect;

import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CdsConnectFile {

  static final Logger logger = LoggerFactory.getLogger(CdsConnectFile.class);

  CdsConnectConnection connection;

  String path = "";
  String filename = "";

  public CdsConnectFile(CdsConnectConnection connection, String path) {
    this.connection = connection;
    this.path = path;
  }

  public CdsConnectFile(CdsConnectConnection connection, JsonElement jsonFileElement) {
    this(connection, jsonFileElement.getAsString());
  }

  public byte[] getCqlBundle() {
    return connection.retrieveCqlBundle(path);
  }

  public String getPath() {
    return path;
  }

  public String getFilename() {
    if (filename.isEmpty()) {
      String[] filenameParts = path.split("/");
      filename = filenameParts[filenameParts.length-1];
    }
    return filename;
  }
}
