package org.hl7.davinci.endpoint.database;

import org.springframework.beans.factory.annotation.Autowired;

public class RemsServiceImpl implements RemsService{
  @Autowired
  private RemsRepository remsRepository;

  @Override
  public Iterable<Rems> findAll() {
    return this.remsRepository.findAll();
  }

  @Override
  public Rems findById(String id) {
    return this.remsRepository.findById(id).get();
  }

  @Override
  public Rems create(Rems remsCompliance) {
    return this.remsRepository.save(remsCompliance);
  }

  @Override
  public Rems edit(Rems remsCompliance) {
    return this.remsRepository.save(remsCompliance);
  }

  @Override
  public void deleteById(String id) {
    this.remsRepository.deleteById(id);
  }

}
