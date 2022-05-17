package org.hl7.davinci.endpoint.rems.database.fhir;

import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;



/**
 * Defines the operations that the fhir server can provide
 */
@Service
@Primary
public class RemsFhirImpl implements RemsFhirService {
    static final Logger logger = LoggerFactory.getLogger(org.hl7.davinci.endpoint.rems.database.fhir.RemsFhirImpl.class);

    @Autowired
    private RemsFhirRepository fhirRepository;

    @Override
    public Iterable<RemsFhir> findAll() {
        return this.fhirRepository.findAll();
    }

    @Override
    public RemsFhir findById(String id) {
        return this.fhirRepository.findById(id).get();
    }

    @Override
    public RemsFhir create(RemsFhir resource) {
        return this.fhirRepository.save(resource);
    }

    @Override
    public RemsFhir edit(RemsFhir resource) {
        return this.fhirRepository.save(resource);
    }

    @Override
    public void deleteById(String id) {
        this.fhirRepository.deleteById(id);
    }

    @Override
    public void logAll() {
        Iterable<RemsFhir> fullLog = findAll();
        for (RemsFhir entry : fullLog) {
            logger.info("fhir server entry: " + entry.toString());
        }
    }

}
