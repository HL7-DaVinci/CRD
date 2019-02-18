package org.hl7.davinci.endpoint.cdsconnect;

import com.google.gson.JsonSyntaxException;
import org.hl7.davinci.endpoint.components.AbstractCrdRuleQuery;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

public class CdsConnectRuleQuery extends AbstractCrdRuleQuery {

  static final Logger logger =
      LoggerFactory.getLogger(CdsConnectRuleQuery.class);

  private CdsConnectConnection connection;

  private CoverageRequirementRuleCriteria criteria;

  public CdsConnectRuleQuery(CdsConnectConnection connection, CoverageRequirementRuleCriteria criteria) {
    this.connection = connection;
    this.criteria = criteria;
  }

  public List<String> getCql() {
    List<String> cqlList = new ArrayList<>();

    try {
      CdsConnectRuleList ruleList = connection.queryForRulesList(criteria.toQueryString());

      try {
        List<CdsConnectArtifact> artifacts = ruleList.getArtifacts();

        for (CdsConnectArtifact artifact : artifacts) {

          List<CdsConnectFile> files = artifact.getFiles();

          for (CdsConnectFile file : files) {
            cqlList.add(file.getCql());
          }
        }

      } catch (JsonSyntaxException e) {
        e.printStackTrace();
      }

    } catch (HttpClientErrorException e) {
      logger.warn("Not Logged In", e);
    }


    return cqlList;
  }
}
