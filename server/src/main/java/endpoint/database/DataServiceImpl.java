package endpoint.database;

import java.util.List;

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
  public List<DMECoverageRequirementRule> findAll() {
    return this.dataRepository.findAll();
  }

  @Override
  public DMECoverageRequirementRule findById(Long id) {
    return this.dataRepository.findById(id).get();
  }

  @Override
  public DMECoverageRequirementRule create(DMECoverageRequirementRule rule) {
    return this.dataRepository.save(rule);
  }

  @Override
  public DMECoverageRequirementRule edit(DMECoverageRequirementRule rule) {
    return this.dataRepository.save(rule);
  }

  @Override
  public void deleteById(Long id) {
    this.dataRepository.deleteById(id);
  }

}
