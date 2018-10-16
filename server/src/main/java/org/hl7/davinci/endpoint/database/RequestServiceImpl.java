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
public class RequestServiceImpl implements RequestService {
  static final Logger logger = LoggerFactory.getLogger(RequestServiceImpl.class);

  @Autowired
  private RequestRepository requestRepository;

  @Override
  public Iterable<RequestLog> findAll() {
    return this.requestRepository.findAll();
  }

  @Override
  public RequestLog findById(Long id) {
    return this.requestRepository.findById(id).get();
  }

  @Override
  public RequestLog create(RequestLog rule) {
    return this.requestRepository.save(rule);
  }

  @Override
  public RequestLog edit(RequestLog rule) {
    return this.requestRepository.save(rule);
  }

  @Override
  public void deleteById(Long id) {
    this.requestRepository.deleteById(id);
  }

  @Override
  public void logAll() {
    Iterable<RequestLog> fullLog = findAll();
    for (RequestLog entry : fullLog) {
      logger.info("request log entry: " + entry.toString());
      for (CoverageRequirementRule rule : entry.getRulesFound()) {
        logger.info("  --> rule found: " + rule.toString());
      }
    }
  }

}
