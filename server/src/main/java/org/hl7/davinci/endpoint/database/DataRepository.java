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
          + "r.ageRangeLow <= :age "
          + "and r.ageRangeHigh >= :age "
          + "and (r.genderCode IS NULL OR r.genderCode = :genderCode) "
          + "and r.equipmentCode = :equipmentCode ")
  List<CoverageRequirementRule> findRules(
      @Param("age") int age,
      @Param("genderCode") Character genderCode,
      @Param("equipmentCode") String equipmentCode);
}
