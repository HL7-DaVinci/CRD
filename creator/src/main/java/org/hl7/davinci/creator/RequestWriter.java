package org.hl7.davinci.creator;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.davinci.CRDRequestCreator;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Parameters;

import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.Calendar;

/**
 * Small application that will write out a sample CRD request in JSON and XML.
 *
 * The first argument to the application is the desired directory to write the files into. If one is not provided,
 * the application will use the current working directory.
 */
public class RequestWriter {
    public static void main(String[] args) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(1970, Calendar.JULY, 4);
        Parameters crdParams = CRDRequestCreator.createRequest(Enumerations.AdministrativeGender.MALE, cal.getTime());
        FhirContext ctx = FhirContext.forR4();
        String outputPath = args[0];
        if (outputPath == null) {
            outputPath = Paths.get(".").toAbsolutePath().normalize().toString();
        }
        FileWriter jsonWriter = new FileWriter(outputPath + "/crd_request.json");
        ctx.newJsonParser().setPrettyPrint(true).encodeResourceToWriter(crdParams, jsonWriter);
        jsonWriter.close();
        System.out.println(ctx.newJsonParser().encodeResourceToString(crdParams));
        FileWriter xmlWriter = new FileWriter(outputPath + "/crd_request.xml");
        ctx.newXmlParser().setPrettyPrint(true).encodeResourceToWriter(crdParams, xmlWriter);
        xmlWriter.close();
    }
}
