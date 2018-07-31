package fhir.restful.database;

import java.util.List;

/**
 * Outlines which methods the database will support.
 */
public interface DataService {
  List<Datum> findAll();

  Datum findById(Long id);

  Datum create(Datum datum);

  Datum edit(Datum datum);

  void deleteById(Long id);
}
