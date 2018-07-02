
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import org.apache.commons.io.Charsets;
import org.hl7.davinci.DaVinciPractitioner;
import org.hl7.davinci.DaVinciSupport;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.hapi.ctx.ValidationSupportChain;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.model.*;

import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ExtensionTest {
    @Test public void testPractitioner() {
        FhirContext fhirContext = FhirContext.forR4();

// Create a FhirInstanceValidator and register it to a validator
        FhirValidator validator = fhirContext.newValidator();

        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        IValidationSupport valSupport = new DaVinciSupport();
        ValidationSupportChain support = new ValidationSupportChain(new DefaultProfileValidationSupport(), valSupport);
        instanceValidator.setValidationSupport(support);

        validator.registerValidatorModule(instanceValidator);

        DaVinciPractitioner dvP = new DaVinciPractitioner();
        dvP.setId("Henlo");
        ValidationResult result = validator.validateWithResult(dvP);

// Do we have any errors or fatal errors?
        System.out.println(result.isSuccessful()); // false

// Show the issues
        for (SingleValidationMessage next : result.getMessages()) {
            System.out.println(" Next issue " + next.getSeverity() + " - " + next.getLocationString() + " - " + next.getMessage());

        }

    }




}
