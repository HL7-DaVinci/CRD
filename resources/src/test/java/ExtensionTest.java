
import org.hl7.davinci.DaVinciPractitioner;

import org.hl7.davinci.ValidationResources;

import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtensionTest {

    private static String profileDir = "../resources/";
    private List<StructureDefinition> definitions;
    private Logger logger = LoggerFactory.getLogger(ExtensionTest.class);
    @Test
    public void testPractitioner() {

        ValidationResources v = new ValidationResources();

        DaVinciPractitioner dvP = new DaVinciPractitioner();
        dvP.setId("Henlo");

        Meta met1 = new Meta().addProfile("http://acme.org/blah");
        Meta met2 = new Meta().addProfile("http://acme.org/blah2");

        Identifier i = new Identifier();
        i.setSystem("http://loinc.org");
        i.setValue("http://hl7.org/fhir/sid/ndc/us-npi");

        dvP.addName().setFamily("Wow").addGiven("Hello");
        dvP.addIdentifier(i);

        dvP.setMeta(met1);

        assertFalse(v.validate(dvP));
        dvP.setMeta(met2);
        assertTrue(v.validate(dvP));




    }




}
