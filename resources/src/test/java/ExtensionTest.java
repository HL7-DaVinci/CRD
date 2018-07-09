
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.davinci.DaVinciPractitioner;
import org.hl7.davinci.DaVinciSupport;
import org.hl7.davinci.ValidationResources;

import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.hapi.ctx.ValidationSupportChain;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ExtensionTest {

    private static String profileDir = "../resources/";
    private List<StructureDefinition> definitions;
    @Test
    public void testPractitioner() {

        ValidationResources v = new ValidationResources();


 //Create a FhirInstanceValidator and register it to a validator
//        FhirValidator validator = fhirContext.newValidator();
//        definitions = ValidationResources.loadFromDirectory(profileDir);
//
//
//        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
//        //instanceValidator.setStructureDefintion(definitions.get(0));
//
//        IValidationSupport valSupport = new DaVinciSupport();
//        ValidationSupportChain support = new ValidationSupportChain(valSupport, new DefaultProfileValidationSupport());
//        instanceValidator.setValidationSupport(support);
//
//        validator.registerValidatorModule(instanceValidator);

        DaVinciPractitioner dvP = new DaVinciPractitioner();
        dvP.setId("Henlo");

        Meta met = new Meta().addProfile("http://acme.org/blah2");
        //dvP.setMeta(met);

        Identifier i = new Identifier();

        i.setSystem("http://loinc.org");


        i.setValue("http://hl7.org/fhir/sid/ndc/us-npi");

        //i.setId("HotDog");
//        CodeableConcept cc = new CodeableConcept();
//        cc.addCoding().setSystem("http://hl7.org/fhir/v2/0203").setCode("PRN");
//        i.setType(cc);


        dvP.addName().setFamily("Wow").addGiven("Hello");

        dvP.addIdentifier(i);

        v.validate(dvP);



    }




}
