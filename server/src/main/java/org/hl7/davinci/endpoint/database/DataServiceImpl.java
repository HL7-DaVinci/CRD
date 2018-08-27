package org.hl7.davinci.endpoint.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;




/**
 * Defines the operations that the Data service can provide.
 */
@Service
@Primary
public class DataServiceImpl implements DataService {

  @Autowired
  private DataRepository dataRepository;

  @Override
  public Iterable<CoverageRequirementRule> findAll() {
    return this.dataRepository.findAll();
  }

  @Override
  public CoverageRequirementRule findById(Long id) {
    return this.dataRepository.findById(id).get();
  }

  @Override
  public CoverageRequirementRule create(CoverageRequirementRule rule) {
    return this.dataRepository.save(rule);
  }

  @Override
  public CoverageRequirementRule edit(CoverageRequirementRule rule) {
    return this.dataRepository.save(rule);
  }

  @Override
  public void deleteById(Long id) {
    this.dataRepository.deleteById(id);
  }

}
