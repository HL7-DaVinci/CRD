package org.hl7.davinci.endpoint.cdsconnect;

import com.google.gson.JsonSyntaxException;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleFinder;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundle;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.ShortNameMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@Profile("cdsConnect")
public class CdsConnectRuleFinder implements CoverageRequirementRuleFinder {

  static final Logger logger =
      LoggerFactory.getLogger(CdsConnectRuleFinder.class);

  @Autowired
  private CdsConnectConnection connection;

  public List<CoverageRequirementRule> findRules(CoverageRequirementRuleCriteria criteria) {
    List<CoverageRequirementRule> ruleList = new ArrayList<>();


    try {
      CdsConnectRuleList cdsConnectRules = connection.queryForRulesList(makeQueryString(criteria));

      List<CdsConnectArtifact> artifacts = cdsConnectRules.getArtifacts();

      for (CdsConnectArtifact artifact : artifacts) {

        List<CdsConnectFile> files = artifact.getFiles();

        //TODO: why are more than one files possible??
        byte[] cqlBundle = files.get(0).getCqlBundle();

        try {
          CoverageRequirementRule rule = new CoverageRequirementRule();
          CqlBundle bundle = CqlBundle.fromZip(cqlBundle);

          rule.setCqlBundle(bundle);
          rule.setCodeSystem(criteria.getCodeSystem());
          rule.setCode(criteria.getCode());
          rule.setPayor(criteria.getPayor());
          ruleList.add(rule);
        } catch (RuntimeException e) {
          logger.info("Error: could not process cql package: " + e.getMessage());
        }
      }

    } catch (JsonSyntaxException e) {
      e.printStackTrace();

    } catch (HttpClientErrorException e) {
      logger.warn("Not Logged In", e);
    }

    return ruleList;
  }

  private String makeQueryString(CoverageRequirementRuleCriteria criteria) {
    String payor = ShortNameMaps.PAYOR_SHORT_NAME_TO_FULL_NAME.inverse().get(criteria.getPayor());
    String codeSystem = ShortNameMaps.CODE_SYSTEM_SHORT_NAME_TO_FULL_NAME.inverse().get(criteria.getCodeSystem());
    return String.format("%s/%s/%s", payor, codeSystem, criteria.getCode());
  }
}
