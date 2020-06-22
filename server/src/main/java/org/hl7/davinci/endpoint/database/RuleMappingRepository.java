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
public interface RuleMappingRepository extends CrudRepository<RuleMapping, Long> {

  @Query(
      "SELECT r FROM RuleMapping r WHERE "
          + "r.payer = :#{#criteria.payor} "
          + "and r.code = :#{#criteria.code} "
          + "and r.codeSystem = :#{#criteria.codeSystem} "
          + "and r.fhirVersion = :#{#criteria.fhirVersion}")
  List<RuleMapping> findRules(
      @Param("criteria") CoverageRequirementRuleCriteria criteria
  );

  @Query(
      "SELECT r FROM RuleMapping r WHERE "
          + "r.topic = :#{#topic} "
          + "and r.fhirVersion = :#{#fhirVersion}")
  List<RuleMapping> findRules(
      @Param("topic") String topic, @Param("fhirVersion") String fhirVersion
  );
}