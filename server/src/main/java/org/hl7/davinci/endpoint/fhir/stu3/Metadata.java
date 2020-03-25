package org.hl7.davinci.endpoint.fhir.stu3;

import java.util.Calendar;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.davinci.stu3.FhirComponents;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.StringType;


/**
 * The metadata creates a CapabilityStatement.
 */

public class Metadata {

  /**
   * Cached CapabilityStatement.
   */
  private CapabilityStatement capabilityStatement = null;

  public String getMetadata(String baseUrl) {
    if (capabilityStatement == null) {
      capabilityStatement = buildCapabilityStatement(baseUrl);
    }
    FhirContext fhirContext = new FhirComponents().getFhirContext();
    return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(capabilityStatement);
  }

  /**
   * Builds the CapabilityStatement describing the Coverage Requirements Discovery Reference
   * Implementation.
   *
   * @return CapabilityStatement - the CapabilityStatement.
   */
  private CapabilityStatement buildCapabilityStatement(String baseUrl) {
    CapabilityStatement metadata = new CapabilityStatement();

    metadata.setTitle("Da Vinci Coverage Requirements Discovery (CRD) Reference Implementation");
    metadata.setStatus(Enumerations.PublicationStatus.DRAFT);
    metadata.setExperimental(true);
    Calendar calendar = Calendar.getInstance();
    calendar.set(2019, 4, 28, 0, 0, 0);
    metadata.setDate(calendar.getTime());
    metadata.setPublisher("Da Vinci");
    metadata.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);
    CapabilityStatement.CapabilityStatementSoftwareComponent software = new CapabilityStatement.CapabilityStatementSoftwareComponent();
    software.setName("https://github.com/HL7-DaVinci/CRD");
    metadata.setSoftware(software);
    CapabilityStatement.CapabilityStatementImplementationComponent implementation = new CapabilityStatement.CapabilityStatementImplementationComponent();
    implementation.setDescription(metadata.getTitle());
    implementation.setUrl(baseUrl + "metadata");
    metadata.setImplementation(implementation);
    metadata.setFhirVersion(org.hl7.fhir.r4.model.Enumerations.FHIRVersion._3_0_0.toString());
    metadata.addFormat("json");
    Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/capabilitystatement-websocket", new StringType("/fhir/stu3"));
    metadata.addExtension(extension);
    metadata.addImplementationGuide("https://build.fhir.org/ig/HL7/davinci-crd/index.html");
    CapabilityStatement.CapabilityStatementRestComponent rest = new CapabilityStatement.CapabilityStatementRestComponent();
    rest.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);
    CapabilityStatement.CapabilityStatementRestSecurityComponent security = new CapabilityStatement.CapabilityStatementRestSecurityComponent();
    security.setCors(true);
    rest.setSecurity(security);

    // Library Resource
    CapabilityStatement.CapabilityStatementRestResourceComponent library = new CapabilityStatement.CapabilityStatementRestResourceComponent();
    library.setType("Library");
    library.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.READ);
    library.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);
    rest.addResource(library);

    // Questionnaire Resource
    CapabilityStatement.CapabilityStatementRestResourceComponent questionnaire = new CapabilityStatement.CapabilityStatementRestResourceComponent();
    questionnaire.setType("Questionnaire");
    questionnaire.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.READ);
    questionnaire.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);
    rest.addResource(questionnaire);

    // ValueSet Resource
    CapabilityStatement.CapabilityStatementRestResourceComponent valueset = new CapabilityStatement.CapabilityStatementRestResourceComponent();
    valueset.setType("ValueSet");
    valueset.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.READ);
    valueset.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);
    rest.addResource(valueset);

    metadata.addRest(rest);

    return metadata;
  }
}
