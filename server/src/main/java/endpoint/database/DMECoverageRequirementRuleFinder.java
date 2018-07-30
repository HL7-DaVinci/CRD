package endpoint.database;

import org.hl7.fhir.r4.model.Enumerations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DMECoverageRequirementRuleFinder {
    @Autowired
    DataRepository repository;

    public DMECoverageRequirementRuleFinder() {
    }

    public DMECoverageRequirementRule findRule(int age, Enumerations.AdministrativeGender gender, String equipmentCode){
        Character genderCode = gender.getDisplay().charAt(0);

        List<DMECoverageRequirementRule> ruleList = repository.findRule(age, genderCode, equipmentCode);
        if (ruleList.size() == 0){
            //TODO: handle differently?
            return null;
        }
        if (ruleList.size() > 1){
            // TODO: raise an error? at least log an error (with the multiple results)
            return null;
        }
        return ruleList.get(0);
    }

}
