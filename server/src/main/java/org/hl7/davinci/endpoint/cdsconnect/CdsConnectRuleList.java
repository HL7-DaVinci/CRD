package org.hl7.davinci.endpoint.cdsconnect;

import org.hl7.ShortNameMaps;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundle;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;



public class CdsConnectRuleList {

  static final Logger logger =
      LoggerFactory.getLogger(CdsConnectRuleList.class);

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

  /**
   * This method creates rules from the rules list without populating the CqlBundle and CqlPackagePath with valid
   * information. It is useful for querying a list of rules without needing the CQL data.
   */
  public List<CoverageRequirementRule> getPartialRules() {
    List<CoverageRequirementRule> rules = new ArrayList<>();

    if (jsonArray.isJsonArray()) {
      for (JsonElement jsonRuleElement : jsonArray) {
        // get the data from the json
        JsonObject jsonRuleObject = jsonRuleElement.getAsJsonObject();
        Integer nodeId = jsonRuleObject.get("nid").getAsInt();
        try {
          String payorShortName = jsonRuleObject.get("field_payer").getAsString();
          String payor = ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.get(payorShortName);
          String codeSystemShortName = jsonRuleObject.get("field_code_system").getAsString();
          String codeSystem = ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.get(codeSystemShortName);
          String code = jsonRuleObject.get("field_erx_code").getAsString();

          // create a new rule and add the relevant data
          CoverageRequirementRule rule = new CoverageRequirementRule();
          rule.setId(nodeId).setPayor(payor).setCodeSystem(codeSystem).setCode(code);

          rule.setCqlPackagePath("unknown");
          rule.setCqlBundle(new CqlBundle());

          rules.add(rule);

        } catch (NullPointerException e) {
          // null pointer...
          logger.info("error: null pointer exception: " + e.getMessage());
        }
      }
    }

    return rules;
  }
}
