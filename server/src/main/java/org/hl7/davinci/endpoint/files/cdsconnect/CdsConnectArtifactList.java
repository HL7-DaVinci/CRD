package org.hl7.davinci.endpoint.files.cdsconnect;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class CdsConnectArtifactList {

  static final Logger logger =
      LoggerFactory.getLogger(CdsConnectArtifactList.class);

  CdsConnectConnection connection;
  JsonArray jsonArray;

  public CdsConnectArtifactList(CdsConnectConnection connection, String data) {
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

  /**
   * This method creates rules from the rules list without populating the CqlBundle and CqlPackagePath with valid
   * information. It is useful for querying a list of rules without needing the CQL data.
   */
  /*
  public List<CoverageRequirementRule> getRulesForDisplay() {
    List<CoverageRequirementRule> rules = new ArrayList<>();

    if (jsonArray.isJsonArray()) {
      for (JsonElement jsonRuleElement : jsonArray) {
        // get the data from the json
        JsonObject jsonRuleObject = jsonRuleElement.getAsJsonObject();
        Integer nodeId = jsonRuleObject.get("nid").getAsInt();

        // create a new rule and add the relevant data
        CoverageRequirementRule rule = new CoverageRequirementRule();
        rule.setId(nodeId);

        if (jsonRuleObject.has("field_payer")) {
          String payorShortName = jsonRuleObject.get("field_payer").getAsString();
          rule.setPayor(ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.get(payorShortName));
        } else {
          logger.info("could not find field_payor, skipping rule with node id " + nodeId);
          continue;
        }

        if (jsonRuleObject.has("field_code_system")) {
          String codeSystemShortName = jsonRuleObject.get("field_code_system").getAsString();
          rule.setCodeSystem(ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.get(codeSystemShortName));
        } else {
          logger.info("could not find field_code_system, skipping rule with node id " + nodeId);
          continue;
        }

        if (jsonRuleObject.has("field_erx_code")) {
          rule.setCode(jsonRuleObject.get("field_erx_code").getAsString());
        } else {
          logger.info("could not find field_erx_code, skipping rule with node id " + nodeId);
          continue;
        }

        if (jsonRuleObject.has("title")) {
          rule.setEditLink(connection.getBaseUrl() + "/node/" + nodeId);
        } else {
          logger.info("could not find title, skipping rule with node id " + nodeId);
          continue;
        }

        rule.setCqlPackagePath("unknown");
        //TODO: fixme
        // CqlBundle emptyCqlBundle = new CqlBundle();
        // rule.setCqlBundle(emptyCqlBundle);
        rules.add(rule);
      }
    }

    return rules;
  }
  */
}
