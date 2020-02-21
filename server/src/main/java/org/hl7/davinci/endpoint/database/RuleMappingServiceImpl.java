package org.hl7.davinci.endpoint.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Defines the operations that the Data service can provide.
 */

@Service
@Primary
public class RuleMappingServiceImpl implements RuleMappingService {

  @Autowired
  private RuleMappingRepository ruleMappingRepository;

  @Override
  public Iterable<RuleMapping> findAll() {
    return this.ruleMappingRepository.findAll();
  }

  @Override
  public RuleMapping findById(Long id) {
    return this.ruleMappingRepository.findById(id).get();
  }

  @Override
  public RuleMapping create(RuleMapping rule) {
    return this.ruleMappingRepository.save(rule);
  }

  @Override
  public RuleMapping edit(RuleMapping rule) {
    return this.ruleMappingRepository.save(rule);
  }

  @Override
  public void deleteById(Long id) {
    this.ruleMappingRepository.deleteById(id);
  }

}