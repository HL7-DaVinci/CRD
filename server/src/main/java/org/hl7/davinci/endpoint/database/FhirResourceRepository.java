package org.hl7.davinci.endpoint.database;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RepositoryRestResource
@CrossOrigin(origins = "http://localhost:4200")
@Repository
public interface FhirResourceRepository extends CrudRepository<FhirResource, Long> {

  @Query(
      "SELECT r FROM FhirResource r WHERE "
          + "r.fhirVersion = :#{#criteria.fhirVersion} "
          + "and LOWER(r.resourceType) = :#{#criteria.resourceType} "
          + "and r.id = :#{#criteria.id}")
  List<FhirResource> findById(
      @Param("criteria") FhirResourceCriteria criteria
  );

  @Query(
      "SELECT r FROM FhirResource r WHERE "
          + "r.fhirVersion = :#{#criteria.fhirVersion} "
          + "and LOWER(r.resourceType) = :#{#criteria.resourceType} "
          + "and LOWER(r.name) = :#{#criteria.name}")
  List<FhirResource> findByName(
      @Param("criteria") FhirResourceCriteria criteria
  );

  @Query(
      "SELECT r FROM FhirResource r WHERE "
          + "r.fhirVersion = :#{#criteria.fhirVersion} "
          + "and LOWER(r.resourceType) = :#{#criteria.resourceType} "
          + "and r.url = :#{#criteria.url}")
  List<FhirResource> findByUrl(
      @Param("criteria") FhirResourceCriteria criteria
  );

  @Query(
      "SELECT r FROM FhirResource r WHERE "
          + "r.fhirVersion = :#{#criteria.fhirVersion} "
          + "and LOWER(r.resourceType) = :#{#criteria.resourceType} "
          + "and LOWER(r.topic) = :#{#criteria.topic}")
  List<FhirResource> findByTopic(
      @Param("criteria") FhirResourceCriteria criteria
  );

  @Query(
      "SELECT r FROM FhirResource r "
          + "order by r.topic, r.id")
  List<FhirResource> findAll();
}