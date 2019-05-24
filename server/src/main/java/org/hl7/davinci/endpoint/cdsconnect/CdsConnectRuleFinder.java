package org.hl7.davinci.endpoint.cdsconnect;

import com.google.gson.JsonSyntaxException;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleFinder;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import java.util.ArrayList;
import java.util.List;

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


  /**
   * Find and return the relevant coverage rule in CdsConnect.
   *
   * @param criteria The search criteria object
   */
  public List<CoverageRequirementRule> findRules(CoverageRequirementRuleCriteria criteria) {
    List<CoverageRequirementRule> ruleList = new ArrayList<>();

    try {
      CdsConnectRuleList cdsConnectRules = connection.queryForRulesList(criteria.getQueryString());

      List<CdsConnectArtifact> artifacts = cdsConnectRules.getArtifacts();

      for (CdsConnectArtifact artifact : artifacts) {

        List<CdsConnectFile> files = artifact.getFiles();

        // grab the first file and ignore the others
        byte[] cqlBundle = files.get(0).getCqlBundle();

        try {
          CoverageRequirementRule rule = new CoverageRequirementRule();
          CqlBundle bundle = CqlBundle.fromZip(cqlBundle);

          rule.setCqlBundle(bundle);
          rule.setCodeSystem(artifact.getCodeSystem());
          rule.setCode(artifact.getCode());
          rule.setPayor(artifact.getPayor());
          rule.setId(artifact.getId());
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

  /**
   * Find and return all coverage rules in CdsConnect.
   * Note: the list of rules returned may not include valid CQL data. This was done as an optimization.
   */
  public List<CoverageRequirementRule> findAll() {

    List<CoverageRequirementRule> ruleList = new ArrayList<>();

    try {
      CdsConnectRuleList cdsConnectRules = connection.queryForRulesList("");

      ruleList = cdsConnectRules.getRulesForDisplay();

    } catch (JsonSyntaxException e) {
      e.printStackTrace();

    } catch (HttpClientErrorException e) {
      logger.warn("Not Logged In", e);
    }

    return ruleList;
  }
}
