package fhir.restful.Database;

import fhir.restful.Database.Datum;

import java.util.List;

/**
 * Outlines which methods the database will support.
 */
public interface DataService {
    List<Datum> findAll();
    Datum findById(Long id);
    Datum create(Datum Datum);
    Datum edit(Datum Datum);
    void deleteById(Long id);
}
