package org.hl7.davinci.endpoint.database;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoverageRequirementRuleFinder {

  static final Logger logger =
      LoggerFactory.getLogger(CoverageRequirementRuleFinder.class);

  @Autowired
  DataRepository repository;

  public CoverageRequirementRuleFinder() {
  }

  /**
   * Find and return the relevant coverage rule in the database.
   *
   * @param age Patient age in years.
   * @param genderCode Patient gender as a character.
   * @param equipmentCode desired code
   * @param codeSystem URL for the code system of the equipmentCode
   */
  public List<CoverageRequirementRule> findRules(
      int age, char genderCode, String equipmentCode, String codeSystem, String patientAddressState,
      String providerAddressState) {
    String queryString = String.format("age=%d, genderCode=%c, equipmentCode=%s, codeSystem=%s, patientAddressState=%s, providerAddressState=%s",
        age, genderCode, equipmentCode, codeSystem, patientAddressState, providerAddressState);

    List<CoverageRequirementRule> ruleList = repository.findRules(
        age, genderCode, equipmentCode, codeSystem, patientAddressState, providerAddressState);
    if (ruleList.size() == 0) {
      logger.debug("RuleFinder returned no results for query: " + queryString);
    }
    return ruleList;
  }
}
