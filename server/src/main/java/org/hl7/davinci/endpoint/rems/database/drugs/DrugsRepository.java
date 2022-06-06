package org.hl7.davinci.endpoint.rems.database.drugs;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.data.repository.query.Param;


import java.util.List;


@RepositoryRestResource
@CrossOrigin(origins = "http://localhost:4200")
@Repository
public interface DrugsRepository extends CrudRepository<Drug, String> {

    @Query(
            "SELECT r FROM Drug r")
    List<Drug> findLogs();

    @Query("SELECT r FROM Drug r where r.codeSystem = :system and r.code = :code")
    List<Drug> findDrugByCode(@Param("system") String system, @Param("code") String code);
}

