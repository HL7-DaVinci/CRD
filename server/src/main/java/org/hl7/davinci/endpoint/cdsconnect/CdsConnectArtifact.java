package org.hl7.davinci.endpoint.cdsconnect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;



public class CdsConnectArtifact {

  CdsConnectConnection connection;
  JsonObject jsonArtifactObject;

  public CdsConnectArtifact(CdsConnectConnection connection, String jsonArtifactString) {
    this.connection = connection;
    jsonArtifactObject = new JsonParser().parse(jsonArtifactString).getAsJsonObject();
  }

  public List<CdsConnectFile> getFiles() {
    List<CdsConnectFile> files = new ArrayList<>();

    JsonElement jsonFiles = jsonArtifactObject.get("artifact_representation").getAsJsonObject()
        .get("logic_files");

    if (jsonFiles.isJsonArray()) {
      JsonArray jsonFilesArray = jsonFiles.getAsJsonArray();
      for (JsonElement jsonFileElement : jsonFilesArray) {
        files.add(new CdsConnectFile(connection, jsonFileElement));
      }
    }

    return files;
  }
}
