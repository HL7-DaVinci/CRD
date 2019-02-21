package org.hl7.davinci.endpoint.database;

import java.util.List;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
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
          + "r.payor = :#{#criteria.payor} "
          + "and r.code = :#{#criteria.code} "
          + "and r.codeSystem = :#{#criteria.codeSystem} ")
  List<CoverageRequirementRule> findRules(
      @Param("criteria") CoverageRequirementRuleCriteria criteria
  );
}
