package org.hl7.davinci.endpoint.database;

public interface ClientService {
  Iterable<Client> findAll();

  Client findById(String id);

  Client create(Client client);

  Client edit(Client client);

  void deleteById(String id);
}
