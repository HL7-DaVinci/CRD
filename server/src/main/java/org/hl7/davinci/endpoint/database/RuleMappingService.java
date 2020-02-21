package org.hl7.davinci.endpoint.database;

/**
 * Outlines which methods the database will support.
 */

public interface RuleMappingService {
  Iterable<RuleMapping> findAll();

  RuleMapping findById(Long id);

  RuleMapping create(RuleMapping rule);

  RuleMapping edit(RuleMapping rule);

  void deleteById(Long id);
}