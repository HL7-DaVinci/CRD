package org.hl7.davinci.endpoint.database;

import org.springframework.beans.factory.annotation.Autowired;

public class ClientServiceImpl implements ClientService{
  @Autowired
  private ClientRepository clientRepository;

  @Override
  public Iterable<Client> findAll() {
    return this.clientRepository.findAll();
  }

  @Override
  public Client findById(String id) {
    return this.clientRepository.findById(id).get();
  }

  @Override
  public Client create(Client client) {
    return this.clientRepository.save(client);
  }

  @Override
  public Client edit(Client client) {
    return this.clientRepository.save(client);
  }

  @Override
  public void deleteById(String id) {
    this.clientRepository.deleteById(id);
  }

}
