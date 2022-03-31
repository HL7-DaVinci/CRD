package org.hl7.davinci.endpoint.database;

public interface RemsService {
  Iterable<Rems> findAll();

  Rems findById(String id);

  Rems create(Rems client);

  Rems edit(Rems client);

  void deleteById(String id);
}
