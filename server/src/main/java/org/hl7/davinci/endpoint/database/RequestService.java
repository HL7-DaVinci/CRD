package org.hl7.davinci.endpoint.database;

/**
 * Outlines which methods the database will support.
 */
public interface RequestService {
  Iterable<RequestLog> findAll();

  RequestLog findById(Long id);

  RequestLog create(RequestLog rule);

  RequestLog edit(RequestLog rule);

  void deleteById(Long id);

  void logAll();
}
