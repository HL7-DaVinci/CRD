package org.hl7.davinci.endpoint.fhir.r4;

import java.util.Calendar;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementImplementationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementKind;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestResourceOperationComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementSoftwareComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.RestfulCapabilityMode;
import org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteraction;
import org.hl7.fhir.r4.model.Enumerations.FHIRVersion;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.CapabilityStatement2.CapabilityStatement2ImplementationComponent;


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
    metadata.setStatus(PublicationStatus.DRAFT);
    metadata.setExperimental(true);
    Calendar calendar = Calendar.getInstance();
    calendar.set(2019, 4, 28, 0, 0, 0);
    metadata.setDate(calendar.getTime());
    metadata.setPublisher("Da Vinci");
    metadata.setKind(CapabilityStatementKind.INSTANCE);
    CapabilityStatementSoftwareComponent software = new CapabilityStatementSoftwareComponent();
    software.setName("https://github.com/HL7-DaVinci/CRD");
    metadata.setSoftware(software);
    CapabilityStatementImplementationComponent implementation = new CapabilityStatementImplementationComponent();
    implementation.setDescription(metadata.getTitle());
    implementation.setUrl(baseUrl + "metadata");
    metadata.setImplementation(implementation);
    metadata.setFhirVersion(FHIRVersion._4_0_1);
    metadata.addFormat("json");
    metadata.addExtension("http://hl7.org/fhir/StructureDefinition/capabilitystatement-websocket", new StringType("/fhir/r4"));
    metadata.addImplementationGuide("https://build.fhir.org/ig/HL7/davinci-crd/index.html");
    CapabilityStatementRestComponent rest = new CapabilityStatementRestComponent();
    rest.setMode(RestfulCapabilityMode.SERVER);
    CapabilityStatementRestSecurityComponent security = new CapabilityStatementRestSecurityComponent();
    security.setCors(true);
    rest.setSecurity(security);

    // Library Resource
    CapabilityStatementRestResourceComponent library = new CapabilityStatementRestResourceComponent();
    library.setType("Library");
    library.addInteraction().setCode(TypeRestfulInteraction.READ);
    library.addInteraction().setCode(TypeRestfulInteraction.SEARCHTYPE);
    library.addInteraction().setCode(TypeRestfulInteraction.CREATE);
    rest.addResource(library);

    // Questionnaire Resource
    CapabilityStatementRestResourceComponent questionnaire = new CapabilityStatementRestResourceComponent();
    questionnaire.setType("Questionnaire");
    questionnaire.addInteraction().setCode(TypeRestfulInteraction.READ);
    questionnaire.addInteraction().setCode(TypeRestfulInteraction.SEARCHTYPE);
    questionnaire.addInteraction().setCode(TypeRestfulInteraction.CREATE);
    CapabilityStatementRestResourceOperationComponent questionnairePackageOperation = new CapabilityStatementRestResourceOperationComponent();
    questionnairePackageOperation.setName("questionnaire-package");
    questionnairePackageOperation.setDefinition("http://hl7.org/fhir/us/davinci-dtr/OperationDefinition/Questionnaire-package");
    questionnairePackageOperation.setDocumentation("Retrieve the Questionnaire(s), Libraries, and Valuesets for a given order and coverage. This operation is to support HL7 DaVinci DTR.");
    questionnaire.addOperation(questionnairePackageOperation);
    CapabilityStatementRestResourceOperationComponent questionnaireNextQuestionOperation = new CapabilityStatementRestResourceOperationComponent();
    questionnaireNextQuestionOperation.setName("next-question");
    questionnaireNextQuestionOperation.setDefinition("http://hl7.org/fhir/uv/sdc/OperationDefinition/Questionnaire-next-question");
    questionnaireNextQuestionOperation.setDocumentation("Retrieve the next question(s) for a given Questionnaire and the answer(s) to the current question(s).");
    questionnaire.addOperation(questionnaireNextQuestionOperation);
    rest.addResource(questionnaire);

    // QuestionnaireResponse Resource
    CapabilityStatementRestResourceComponent questionnaireResponse = new CapabilityStatementRestResourceComponent();
    questionnaireResponse.setType("QuestionnaireResponse");
    questionnaireResponse.addInteraction().setCode(TypeRestfulInteraction.READ);
    questionnaireResponse.addInteraction().setCode(TypeRestfulInteraction.SEARCHTYPE);
    questionnaireResponse.addInteraction().setCode(TypeRestfulInteraction.CREATE);
    rest.addResource(questionnaireResponse);

    // ValueSet Resource
    CapabilityStatementRestResourceComponent valueset = new CapabilityStatementRestResourceComponent();
    valueset.setType("ValueSet");
    valueset.addInteraction().setCode(TypeRestfulInteraction.READ);
    valueset.addInteraction().setCode(TypeRestfulInteraction.SEARCHTYPE);
    valueset.addInteraction().setCode(TypeRestfulInteraction.CREATE);
    // ValueSet $expand Operator
    CapabilityStatementRestResourceOperationComponent expandOperator = new CapabilityStatementRestResourceOperationComponent();
    expandOperator.setName("expand");
    expandOperator.setDefinition("http://hl7.org/fhir/OperationDefinition/ValueSet-expand");
    expandOperator.setDocumentation("Only works at the ValueSet type level with a 'url' query parameter. Will only return expansions that are pre-cached on this server.");
    valueset.addOperation(expandOperator);
    rest.addResource(valueset);

    metadata.addRest(rest);

    return metadata;
  }
}
