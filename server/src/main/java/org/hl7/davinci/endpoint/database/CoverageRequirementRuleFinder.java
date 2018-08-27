package org.hl7.davinci.endpoint.database;

import org.hl7.davinci.endpoint.CoverageRequirementsDiscoveryOperation;
import java.util.List;

import org.hl7.fhir.r4.model.Enumerations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoverageRequirementRuleFinder {
  static final Logger logger =
      LoggerFactory.getLogger(CoverageRequirementsDiscoveryOperation.class);

  @Autowired DataRepository repository;

  public CoverageRequirementRuleFinder() {}

  /**
   * Find and return the relevant coverage rule in the database.
   *
   * @param age Patient age in years.
   * @param gender Patient gender (enum from hapi fhir).
   * @param equipmentCode CPT codes for now.
   * @return
   */
  public CoverageRequirementRule findRule(
      int age, Enumerations.AdministrativeGender gender, String equipmentCode) {
    Character genderCode = gender.getDisplay().charAt(0);
    String queryString =
        String.format("age=%d, genderCode=%c, equipmentCode=%s", age, genderCode, equipmentCode);

    List<CoverageRequirementRule> ruleList = repository.findRule(age, genderCode, equipmentCode);
    if (ruleList.size() == 0) {
      logger.debug("RuleFinder returned no results for query: " + queryString);
      return null;
    }
    if (ruleList.size() > 1) {
      logger.error("RuleFinder returned MULTIPLE results for query: " + queryString);
      return null;
    }
    return ruleList.get(0);
  }
}
