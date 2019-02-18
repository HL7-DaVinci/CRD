package org.hl7.davinci.endpoint.cdsconnect;

import com.google.gson.*;
import java.util.ArrayList;
import java.util.List;



public class CdsConnectRuleList {

  CdsConnectConnection connection;
  JsonArray jsonArray;

  public CdsConnectRuleList(CdsConnectConnection connection, String data) {
    this.connection = connection;
    this.jsonArray = new JsonParser().parse(data).getAsJsonArray();

  }

  public List<CdsConnectArtifact> getArtifacts() {
    List<CdsConnectArtifact> artifacts = new ArrayList<>();

    if (jsonArray.isJsonArray()) {
      for (JsonElement jsonElement : jsonArray) {

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Integer nodeId = jsonObject.get("nid").getAsInt();

        artifacts.add(new CdsConnectArtifact(connection, connection.retrieveArtifact(nodeId)));
      }
    }

    return artifacts;
  }
}
