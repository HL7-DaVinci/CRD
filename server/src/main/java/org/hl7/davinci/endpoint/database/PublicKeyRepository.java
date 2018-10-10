package org.hl7.davinci.endpoint.database;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;


@RepositoryRestResource
@CrossOrigin(origins = "http://localhost:4200")
@Repository
public interface PublicKeyRepository extends CrudRepository<PublicKey, String> {

  @Query(
      "SELECT r FROM PublicKey r")
  List<PublicKey> findKeys();
}