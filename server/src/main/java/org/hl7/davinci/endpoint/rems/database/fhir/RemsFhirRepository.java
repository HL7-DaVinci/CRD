package org.hl7.davinci.endpoint.rems.database.fhir;

import java.util.List;

import org.hl7.fhir.r4.model.IdType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;


@RepositoryRestResource
@CrossOrigin(origins = "http://localhost:4200")
@Repository
public interface RemsFhirRepository extends CrudRepository<RemsFhir, String> {

    @Query(
            "SELECT r FROM RemsFhir r")
    List<RemsFhir> findLogs();

}


