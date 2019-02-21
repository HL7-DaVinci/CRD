package org.hl7.davinci.endpoint.cdsconnect;

import com.google.gson.JsonSyntaxException;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleFinder;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

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

      try {
        List<CdsConnectArtifact> artifacts = cdsConnectRules.getArtifacts();

        for (CdsConnectArtifact artifact : artifacts) {

          List<CdsConnectFile> files = artifact.getFiles();

          //TODO: why are more than one files possible??
          String cqlFile = files.get(0).getCql();

          CoverageRequirementRule rule = new CoverageRequirementRule();
          rule.setCql(cqlFile);
          rule.setCodeSystem(criteria.getCodeSystem());
          rule.setCode(criteria.getCode());
          rule.setPayor(criteria.getPayor());
          ruleList.add(rule);
        }

      } catch (JsonSyntaxException e) {
        e.printStackTrace();
      }

    } catch (HttpClientErrorException e) {
      logger.warn("Not Logged In", e);
    }

    return ruleList;
  }

  private String makeQueryString(CoverageRequirementRuleCriteria criteria) {
    return String.format("%s/%s/%s", criteria.getPayor(), criteria.getCodeSystem(), criteria.getCode());
  }
}
