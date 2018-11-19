package org.hl7.davinci.endpoint.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


/**
 * Defines the operations that the request log service can provide.
 */
@Service
@Primary
public class PublicKeyServiceImpl implements PublicKeyService {
  static final Logger logger = LoggerFactory.getLogger(PublicKeyServiceImpl.class);

  @Autowired
  private PublicKeyRepository publicKeyRepository;

  @Override
  public Iterable<PublicKey> findAll() {
    return this.publicKeyRepository.findAll();
  }

  @Override
  public PublicKey findById(String id) {
    return this.publicKeyRepository.findById(id).get();
  }

  @Override
  public PublicKey create(PublicKey rule) {
    return this.publicKeyRepository.save(rule);
  }

  @Override
  public PublicKey edit(PublicKey rule) {
    return this.publicKeyRepository.save(rule);
  }

  @Override
  public void deleteById(String id) {
    this.publicKeyRepository.deleteById(id);
  }

}