package org.hl7.davinci.endpoint.database;


/**
 * Outlines which methods the database will support.
 */
public interface PublicKeyService {
  Iterable<PublicKey> findAll();

  PublicKey findById(String id);

  PublicKey create(PublicKey rule);

  PublicKey edit(PublicKey rule);

  void deleteById(String id);

}