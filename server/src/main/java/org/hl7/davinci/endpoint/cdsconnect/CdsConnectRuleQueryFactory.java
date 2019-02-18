package org.hl7.davinci.endpoint.cdsconnect;

import org.hl7.davinci.endpoint.components.AbstractCrdRuleQuery;
import org.hl7.davinci.endpoint.components.AbstractCrdRuleQueryFactory;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleCriteria;

public class CdsConnectRuleQueryFactory extends AbstractCrdRuleQueryFactory {

  private CdsConnectConnection connection;

  public CdsConnectRuleQueryFactory(CdsConnectConnection connection) {
    this.connection = connection;
  }

  public AbstractCrdRuleQuery create(CoverageRequirementRuleCriteria criteria) {
    return new CdsConnectRuleQuery(connection, criteria);
  }
}
