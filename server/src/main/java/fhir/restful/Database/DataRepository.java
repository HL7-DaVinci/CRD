package fhir.restful.Database;



import fhir.restful.Database.Datum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

@RepositoryRestResource
@CrossOrigin(origins = "http://localhost:4200")
public interface DataRepository extends JpaRepository<Datum,Long> {

}
