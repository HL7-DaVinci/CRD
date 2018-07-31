package endpoint.database;

import java.util.List;
import org.hl7.fhir.r4.model.Enumerations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoverageRequirementRuleFinder {
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

    List<CoverageRequirementRule> ruleList = repository.findRule(age, genderCode, equipmentCode);
    if (ruleList.size() == 0) {
      // TODO: handle differently?
      return null;
    }
    if (ruleList.size() > 1) {
      // TODO: raise an error? at least log an error (with the multiple results)
      return null;
    }
    return ruleList.get(0);
  }
}
