package org.hl7.davinci.endpoint.files.cdsconnect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;



public class CdsConnectArtifact {

  static final Logger logger = LoggerFactory.getLogger(CdsConnectArtifact.class);

  private CdsConnectConnection connection;
  private JsonObject jsonArtifactObject;

  private JsonObject meta;
  private JsonObject crdData;

  public CdsConnectArtifact(CdsConnectConnection connection, String jsonArtifactString) {
    this.connection = connection;
    jsonArtifactObject = new JsonParser().parse(jsonArtifactString).getAsJsonObject();
    meta = jsonArtifactObject.get("meta").getAsJsonObject();
    crdData = jsonArtifactObject.get("coverage_requirements_discovery").getAsJsonObject();
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

  /**
   * Gets the shortname of the payor (ie. cms).
   */
  public String getPayor() {
    return crdData.get("payer").getAsString();
  }

  /**
   * Gets the shortname version of the code system (ie. cpt).
   */
  public String getCodeSystem() {
    return crdData.get("code_system").getAsString();
  }

  public String getCode() {
    return crdData.get("electronic_prescribing_code").getAsString();
  }

  public Integer getId() {
    return meta.get("node_id").getAsInt();
  }

  public String getTopicMetadata() {
    try {
      String topicMetadata = jsonArtifactObject.get("artifact_representation").getAsJsonObject().get("inclusions").getAsString();

      // remove the <p> and </p> tags
      topicMetadata = topicMetadata.replaceAll("<p>", "");
      topicMetadata = topicMetadata.replaceAll("</p>", "");

      return topicMetadata;

    } catch (Exception e) {
      logger.warn("failed to find TopicMetadata");
      return "";
    }

  }
}
