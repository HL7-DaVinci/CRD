package org.hl7.davinci.endpoint.rems.database.requirement;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;


@RepositoryRestResource
@CrossOrigin(origins = "http://localhost:4200")
@Repository
public interface MetRequirementRepository extends CrudRepository<MetRequirement, String> {

    @Query(
            "SELECT r FROM MetRequirement r")
    List<MetRequirement> findAll();

}


