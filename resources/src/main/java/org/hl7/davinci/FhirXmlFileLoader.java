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

public class FhirXmlFileLoader {

    public static <T extends IBaseResource> List<T> loadFromDirectory(String rootDir) {
        IParser xmlParser = FhirContext.forR4().newXmlParser();
        xmlParser.setParserErrorHandler(new StrictErrorHandler());
        List<T> definitions = new ArrayList<>();
        System.out.println(rootDir);
        File[] profiles =
                new File(FhirXmlFileLoader.class.getClassLoader().getResource(rootDir).getFile()).listFiles();

        Arrays.asList(profiles).forEach(f -> {
            try {
                T sd = (T) xmlParser.parseResource(new FileReader(f));
                definitions.add(sd);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        return definitions;
    }
}