import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.hl7.davinci.r4.fhirresources.DaVinciPractitioner;
import org.hl7.davinci.r4.validation.ValidationResources;

import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.StructureDefinition;

import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionTest {

  private static String profileDir = "../resources/";
  private List<StructureDefinition> definitions;
  private Logger logger = LoggerFactory.getLogger(ExtensionTest.class);

  @Test
  public void testPractitioner() {
    DaVinciPractitioner dvP = new DaVinciPractitioner();
    dvP.setId("Henlo");

    Identifier i = new Identifier();
    i.setSystem("http://loinc.org");
    i.setValue("http://hl7.org/fhir/sid/ndc/us-npi");

    dvP.addName().setFamily("Wow").addGiven("Hello");
    dvP.addIdentifier(i);

    Meta met1 = new Meta().addProfile("http://acme.org/blah");
    dvP.setMeta(met1);

    ValidationResources v = new ValidationResources();
    assertFalse(v.validate(dvP));

    Meta met2 = new Meta().addProfile("http://acme.org/blah2");
    dvP.setMeta(met2);
    assertTrue(v.validate(dvP));
  }
}
