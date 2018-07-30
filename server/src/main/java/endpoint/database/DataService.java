package endpoint.database;

import java.util.List;

/**
 * Outlines which methods the database will support.
 */
public interface DataService {
  List<DMECoverageRequirementRule> findAll();

  DMECoverageRequirementRule findById(Long id);

  DMECoverageRequirementRule create(DMECoverageRequirementRule rule);

  DMECoverageRequirementRule edit(DMECoverageRequirementRule rule);

  void deleteById(Long id);
}
