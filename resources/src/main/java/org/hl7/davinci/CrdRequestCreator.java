package org.hl7.davinci;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.EligibilityRequest;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;


/**
 * CrdRequestCreator is a class that creates example CRD requests in the form of FHIR Parameters.
 */
public class CrdRequestCreator {

  /**
   * Generate a request.
   *
   * @param patientGender    Desired gender of the patient in the request
   * @param patientBirthdate Desired birthdate of the patient in the request
   * @return Fully populated Parameters object
   */
  public static Parameters createRequest(Enumerations.AdministrativeGender patientGender,
                                         Date patientBirthdate) {

    // create an EligibilityRequest object with ID set
    DaVinciEligibilityRequest eligibilityRequest = new DaVinciEligibilityRequest();

    eligibilityRequest.setId(idString());
    eligibilityRequest.setStatus(EligibilityRequest.EligibilityRequestStatus.ACTIVE);

    // create a Patient object with Name set
    Patient patient = new Patient();
    patient.setId(idString());
    patient.setGender(patientGender);
    patient.setBirthDate(patientBirthdate);
    eligibilityRequest.setPatient(generateReference(patient));

    // create a Practitioner object with ID set
    Practitioner provider = new Practitioner();
    provider.setId(idString());
    provider.addIdentifier(new Identifier().setSystem("http://hl7.org/fhir/sid/us-npi").setValue("1122334455"));
    provider.addName(new HumanName().addGiven("Jane").setFamily("Doe").addPrefix("Dr."));
    eligibilityRequest.setProvider(generateReference(provider));

    // create an Organization object with ID and Name set
    Organization insurer = new Organization();
    insurer.setId(idString());
    insurer.setName("Centers for Medicare and Medicaid Services");
    eligibilityRequest.setInsurer(generateReference(insurer));

    // create a Location Object
    Location facility = new Location();
    facility.setId(idString());
    facility.setAddress(new Address().addLine("100 Good St")
        .setCity("Bedford")
        .setState("MA")
        .setPostalCode("01730"));

    eligibilityRequest.setFacility(generateReference(facility));

    // create a Coverage object with ID set
    Coverage coverage = new Coverage();
    coverage.setId(idString());
    Coding planCode = new Coding().setCode("plan").setSystem("http://hl7.org/fhir/coverage-class");
    Coverage.ClassComponent coverageClass = new Coverage.ClassComponent();
    coverageClass.setType(planCode).setValue("Medicare Part D");
    coverage.addClass_(coverageClass);
    eligibilityRequest.setCoverage(generateReference(coverage));

    // create a Condition for the patientContext
    Condition condition = new Condition();
    condition.setId(idString());
    condition.setSubject(generateReference(patient));
    condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
    Coding kneeCoding = new Coding().setCode("M23.51").setSystem("http://hl7.org/fhir/sid/icd-10")
        .setDisplay("Instability of the right knee");
    condition.setCode(new CodeableConcept().addCoding(kneeCoding));

    DaVinciEligibilityRequest.ServiceInformation serviceInformation = new DaVinciEligibilityRequest
        .ServiceInformation();

    Coding walker = new Coding().setCode("E0130").setSystem("http://www.ama-assn.org/go/cpt").setDisplay("Walker");
    serviceInformation.setServiceRequestType(new CodeableConcept().addCoding(walker));
    List<Reference> patientContext = new ArrayList<>();
    patientContext.add(generateReference(condition));
    serviceInformation.setPatientContext(patientContext);
    List<DaVinciEligibilityRequest.ServiceInformation> siList = new ArrayList<>();
    siList.add(serviceInformation);
    eligibilityRequest.setServiceInformation(siList);

    // build the parameters for the CRD
    Parameters crdParams = new Parameters();

    // build the request parameter
    Parameters.ParametersParameterComponent param = crdParams.addParameter();
    param.setName("request");
    param.addPart().setName("eligibilityrequest").setResource(eligibilityRequest);
    param.addPart().setName("patient").setResource(patient);
    param.addPart().setName("coverage").setResource(coverage);
    param.addPart().setName("provider").setResource(provider);
    param.addPart().setName("insurer").setResource(insurer);
    param.addPart().setName("facility").setResource(facility);
    param.addPart().setName("patientContext").setResource(condition);

    // create and add an Endpoint object to the CRD parameters
    Endpoint endpoint = new Endpoint();
    crdParams.addParameter().setName("endpoint").setResource(endpoint);

    // create and add a CodeableConcept object to the CRD parameters
    CodeableConcept requestQualification = new CodeableConcept();
    crdParams.addParameter().setName("requestQualification").setValue(requestQualification);

    return crdParams;
  }

  public static String idString() {
    UUID uuid = UUID.randomUUID();
    return uuid.toString();
  }

  public static Reference generateReference(Resource resource) {
    Reference reference = new Reference();
    return reference.setReference(String.format("urn:uuid:%s", resource.getId()));
  }
}
