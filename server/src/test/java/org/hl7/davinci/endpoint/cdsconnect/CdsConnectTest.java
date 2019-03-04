package org.hl7.davinci.endpoint.cdsconnect;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class CdsConnectTest {

  static final Logger logger =
      LoggerFactory.getLogger(CdsConnectTest.class);

  @Test
  public void testExecute() {
    /* TODO: implement a test that does not depend on an instance of CDS Connect running
    */

    /*
    CdsConnectConnection cdsConnectConnection = new CdsConnectConnection("http://cdsc-dev.dd:8083",
        "admin", "admin");

    AbstractCrdRuleQueryFactory ruleQueryFactory = new CdsConnectRuleQueryFactory(cdsConnectConnection);

    CoverageRequirementRuleCriteria criteria = new CoverageRequirementRuleCriteria();

    criteria.setPayor("cms").setCodeSystem("cpt").setCode("82947");
    AbstractCrdRuleQuery ruleQuery = ruleQueryFactory.create(criteria);
    List<String> cql = ruleQuery.getCqlBundle();
    assertEquals(1, cql.size());

    criteria.setPayor("cms").setCodeSystem("cpt").setCode("94660");
    ruleQuery = ruleQueryFactory.create(criteria);
    cql = ruleQuery.getCqlBundle();
    assertEquals(1, cql.size());

    criteria.setPayor("cms").setCodeSystem("cpt").setCode("");
    ruleQuery = ruleQueryFactory.create(criteria);
    cql = ruleQuery.getCqlBundle();
    assertEquals(2, cql.size());

    criteria.setPayor("crd").setCodeSystem("cpt").setCode("56778");
    ruleQuery = ruleQueryFactory.create(criteria);
    cql = ruleQuery.getCqlBundle();
    assertEquals(0, cql.size());

    cdsConnectConnection.logout();
    */
  }
}
