package org.hl7.davinci.endpoint.rems.database.fhir;

/**
 * Outlines which methods the database will support.
 */
public interface RemsFhirService {
    Iterable<RemsFhir> findAll();

    RemsFhir findById(String id);

    RemsFhir create(RemsFhir resource);

    RemsFhir edit(RemsFhir resource);

    void deleteById(String id);

    void logAll();
}
