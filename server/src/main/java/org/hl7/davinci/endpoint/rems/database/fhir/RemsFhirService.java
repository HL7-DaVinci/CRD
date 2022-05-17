package org.hl7.davinci.endpoint.rems.database.fhir;

import ca.uhn.fhir.model.api.IResource;
import org.hl7.fhir.r4.model.IdType;

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
