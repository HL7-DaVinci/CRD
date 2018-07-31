package fhir.restful.database;

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
  public List<Datum> findAll() {
    return this.dataRepository.findAll();
  }

  @Override
  public Datum findById(Long id) {
    return this.dataRepository.findById(id).get();
  }

  @Override
  public Datum create(Datum datum) {
    return this.dataRepository.save(datum);
  }

  @Override
  public Datum edit(Datum datum) {
    return this.dataRepository.save(datum);
  }

  @Override
  public void deleteById(Long id) {
    this.dataRepository.deleteById(id);
  }

}
