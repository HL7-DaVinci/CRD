package org.hl7.davinci;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.StructureDefinition;

public class FhirXmlFileLoader {

    public static List<StructureDefinition> loadFromDirectory(String rootDir) {
        IParser xmlParser = FhirContext.forR4().newXmlParser();
        xmlParser.setParserErrorHandler(new StrictErrorHandler());
        List<StructureDefinition> definitions = new ArrayList<>();

        // Check directory for all available structure definitions
        File[] profiles =
                new File(FhirXmlFileLoader.class.getClassLoader().getResource(rootDir).getFile()).listFiles();

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
}