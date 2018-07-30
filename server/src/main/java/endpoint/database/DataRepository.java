package endpoint.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;


@RepositoryRestResource
@CrossOrigin(origins = "http://localhost:4200")
@Repository
public interface DataRepository extends JpaRepository<DMECoverageRequirementRule,Long> {

    @Query("SELECT r FROM DMECoverageRequirementRule r WHERE " +
            "r.ageRangeLow <= :age " +
            "and r.ageRangeHigh >= :age " +
            "and r.genderCode = :genderCode " +
            "and r.equipmentCode = :equipmentCode ")
    List<DMECoverageRequirementRule> findRule(@Param("age") int age, @Param("genderCode") Character genderCode, @Param("equipmentCode") String equipmentCode);

}
