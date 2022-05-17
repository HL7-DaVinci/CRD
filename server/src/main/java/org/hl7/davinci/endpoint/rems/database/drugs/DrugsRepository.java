package org.hl7.davinci.endpoint.rems.database.drugs;

import java.util.List;

import org.hl7.davinci.endpoint.rems.database.fhir.RemsFhir;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;


@RepositoryRestResource
@CrossOrigin(origins = "http://localhost:4200")
@Repository
public interface DrugsRepository extends CrudRepository<Drug, String> {

    @Query(
            "SELECT r FROM Drug r")
    List<Drug> findLogs();

}

