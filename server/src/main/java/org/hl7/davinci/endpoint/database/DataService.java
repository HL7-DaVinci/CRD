package org.hl7.davinci.endpoint.database;

/**
 * Outlines which methods the database will support.
 */
public interface DataService {
  Iterable<CoverageRequirementRule> findAll();

  CoverageRequirementRule findById(Long id);

  CoverageRequirementRule create(CoverageRequirementRule rule);

  CoverageRequirementRule edit(CoverageRequirementRule rule);

  void deleteById(Long id);
}
