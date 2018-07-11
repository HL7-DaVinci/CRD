package org.hl7.davinci;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.StructureDefinition;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.ctx.IValidationSupport;
import org.hl7.fhir.r4.hapi.ctx.ValidationSupportChain;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;

public class ValidationResources {
    private FhirContext ctx;
    private FhirValidator validator;
    private FhirInstanceValidator instanceValidator;
    final Logger logger = LoggerFactory.getLogger(ValidationResources.class);



    public ValidationResources(){

        //Only support for r4 for now
        ctx =  FhirContext.forR4();
        validator = ctx.newValidator();
        instanceValidator = new FhirInstanceValidator();
        IValidationSupport valSupport = new DaVinciValidationSupport();
        ValidationSupportChain support = new ValidationSupportChain(valSupport, new DefaultProfileValidationSupport());
        instanceValidator.setValidationSupport(support);
        validator.registerValidatorModule(instanceValidator);

    }

    public static List<StructureDefinition> loadFromDirectory(String rootDir) {

        IParser xmlParser = FhirContext.forR4().newXmlParser();
        xmlParser.setParserErrorHandler(new StrictErrorHandler());
        List<StructureDefinition> definitions = new ArrayList<>();
        
        File[] profiles =
                new File(ValidationResources.class.getClassLoader().getResource(rootDir).getFile()).listFiles();

        Arrays.asList(profiles).forEach(f -> {
            try {
                StructureDefinition sd = xmlParser.parseResource(StructureDefinition.class, new FileReader(f));
                definitions.add(sd);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        return definitions;
    }

    public boolean validate(IBaseResource theResource){
        ValidationResult result = validator.validateWithResult(theResource);


        // Do we have any errors or fatal errors?
        boolean retVal = result.isSuccessful();
        if(retVal){
            logger.info("Validation success for {}.",theResource.getMeta().getProfile().get(0));
        }else{
            logger.warn("Validation failure for {}.",theResource.getMeta().getProfile().get(0));
        }


        // Show the issues
        for (SingleValidationMessage next : result.getMessages()) {
            switch(next.getSeverity()){
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