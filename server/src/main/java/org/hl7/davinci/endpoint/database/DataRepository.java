package org.hl7.davinci.endpoint.database;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;


@RepositoryRestResource
@CrossOrigin(origins = "http://localhost:4200")
@Repository
public interface DataRepository extends CrudRepository<CoverageRequirementRule, Long> {

  @Query(
      "SELECT r FROM CoverageRequirementRule r WHERE "
          + "r.ageRangeLow <= :#{#criteria.age} "
          + "and r.ageRangeHigh >= :#{#criteria.age} "
          + "and (r.genderCode IS NULL OR r.genderCode = :#{#criteria.genderCode}) "
          + "and (r.patientAddressState IS NULL OR r.patientAddressState = :#{#criteria.patientAddressState}) "
          + "and (r.providerAddressState IS NULL OR r.providerAddressState = :#{#criteria.providerAddressState}) "
          + "and r.equipmentCode = :#{#criteria.equipmentCode} "
          + "and r.codeSystem = :#{#criteria.codeSystem} ")
  List<CoverageRequirementRule> findRules(
      @Param("criteria") CoverageRequirementRuleCriteria criteria
  );
}
