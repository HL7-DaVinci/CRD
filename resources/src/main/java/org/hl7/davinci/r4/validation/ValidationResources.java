package org.hl7.davinci.r4.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.hapi.ctx.ValidationSupportChain;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ValidationResources {
  private FhirValidator validator;
  private static final Logger logger = LoggerFactory.getLogger(ValidationResources.class);


  /**
   * Constructor for the class that creates the context and validator for usage by the
   * rest of the program.
   */
  public ValidationResources() {

    //Only support for r4 for now
    FhirContext ctx = FhirContext.forR4();
    validator = ctx.newValidator();
    FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
    IValidationSupport valSupport = new DaVinciValidationSupport();
    ValidationSupportChain support = new ValidationSupportChain(valSupport,
        new DefaultProfileValidationSupport());
    instanceValidator.setValidationSupport(support);
    validator.registerValidatorModule(instanceValidator);

  }

  /**
   * Loads the structure definitions from the given directory.
   * @param rootDir the directory to load structure definitions from
   * @return a list of structure definitions
   */
  static List<StructureDefinition> loadFromDirectory(String rootDir) {

    IParser xmlParser = FhirContext.forR4().newXmlParser();
    xmlParser.setParserErrorHandler(new StrictErrorHandler());
    List<StructureDefinition> definitions = new ArrayList<>();

    File[] profiles =
        new File(Objects.requireNonNull(ValidationResources.class.getClassLoader()
            .getResource(rootDir))
            .getFile())
            .listFiles();

    assert profiles != null;
    Arrays.asList(profiles).forEach(f -> {
      try {
        StructureDefinition sd = xmlParser.parseResource(StructureDefinition.class,
            new FileReader(f));
        definitions.add(sd);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    });

    return definitions;
  }

  /**
   * Runs the validator on a given resource and deals with response.
   * @param theResource the resource to be validated
   * @return whether the validation was successful or unsuccessful
   */
  public boolean validate(IBaseResource theResource) {
    ValidationResult result = validator.validateWithResult(theResource);


    // Do we have any errors or fatal errors?
    boolean retVal = result.isSuccessful();
    if (retVal) {
      logger.info("Validation success for {}.", theResource.getMeta().getProfile().get(0));
    } else {
      logger.warn("Validation failure for {}.", theResource.getMeta().getProfile().get(0));
    }


    // Show the issues
    for (SingleValidationMessage next : result.getMessages()) {
      switch (next.getSeverity()) {
        case ERROR:
          logger.error(next.getLocationString() + " - " + next.getMessage());
          break;
        case INFORMATION:
          logger.info(next.getLocationString() + " - " + next.getMessage());
          break;
        case WARNING:
          logger.warn(next.getLocationString() + " - " + next.getMessage());
          break;
        case FATAL:
          logger.error(next.getLocationString() + " - " + next.getMessage());
          break;
        default:
          logger.debug(next.getLocationString() + " - " + next.getMessage());
      }
    }

    return retVal;

  }


}