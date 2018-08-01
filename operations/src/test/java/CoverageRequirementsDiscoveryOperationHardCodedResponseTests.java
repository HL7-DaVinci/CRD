import org.hl7.davinci.CoverageRequirementsDiscoveryOperationHardCodedResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CoverageRequirementsDiscoveryOperationHardCodedResponseTests {

    @Test
    @DisplayName("Test CRD with good data.")
    void testCRDSuccess() {
        CoverageRequirementsDiscoveryOperationHardCodedResponse crdop = new CoverageRequirementsDiscoveryOperationHardCodedResponse();

        // valid parameters
        Parameters.ParametersParameterComponent request = buildRequest(
                buildEligibilityRequest("1234"),
                buildPatient("patient-4", "Bob Smith"),
                buildCoverage("4321"),
                buildProvider("5678"),
                buildInsurer("87654", "InsureCo"),
                buildFacility(),
                buildCondition("condition-1"),
                buildDevice("XYZ-123"),
                buildProcedure("12345678"),
                buildMedication(40) );
        Endpoint endpoint = buildEndpoint();
        CodeableConcept requestQualification = buildRequestQualification();
        Parameters outParams = crdop.coverageRequirementsDiscovery(request, endpoint, requestQualification);
        assertFalse(outParams.isEmpty());
    }

    @Test
    @DisplayName("Test CRD with bad data.")
    void testCRDFail() {
        CoverageRequirementsDiscoveryOperationHardCodedResponse crdop = new CoverageRequirementsDiscoveryOperationHardCodedResponse();

        // empty parameters
        Parameters.ParametersParameterComponent request = new Parameters.ParametersParameterComponent();
        Endpoint endpoint = buildEndpoint();
        CodeableConcept requestQualification = buildRequestQualification();
        Parameters outParams = crdop.coverageRequirementsDiscovery(request, endpoint, requestQualification);
        assertTrue(outParams.isEmpty());

        // missing eligibilityRequest
        request = buildRequest(
                null,
                buildPatient("patient-4", "Bob Smith"),
                buildCoverage("4321"),
                buildProvider("5678"),
                buildInsurer("87654", "InsureCo"),
                buildFacility(),
                buildCondition("condition-1"),
                buildDevice("XYZ-123"),
                buildProcedure("12345678"),
                buildMedication(40) );
        outParams = crdop.coverageRequirementsDiscovery(request, endpoint, requestQualification);
        assertTrue(outParams.isEmpty());

        // missing patient
        request = buildRequest(
                buildEligibilityRequest("1234"),
                null,
                buildCoverage("4321"),
                buildProvider("5678"),
                buildInsurer("87654", "InsureCo"),
                buildFacility(),
                buildCondition("condition-1"),
                buildDevice("XYZ-123"),
                buildProcedure("12345678"),
                buildMedication(40) );
        outParams = crdop.coverageRequirementsDiscovery(request, endpoint, requestQualification);
        assertTrue(outParams.isEmpty());

        // missing coverage
        request = buildRequest(
                buildEligibilityRequest("1234"),
                buildPatient("patient-4", "Bob Smith"),
                null,
                buildProvider("5678"),
                buildInsurer("87654", "InsureCo"),
                buildFacility(),
                buildCondition("condition-1"),
                buildDevice("XYZ-123"),
                buildProcedure("12345678"),
                buildMedication(40) );
        outParams = crdop.coverageRequirementsDiscovery(request, endpoint, requestQualification);
        assertTrue(outParams.isEmpty());

        // missing provider
        request = buildRequest(
                buildEligibilityRequest("1234"),
                buildPatient("patient-4", "Bob Smith"),
                buildCoverage("4321"),
                null,
                buildInsurer("87654", "InsureCo"),
                buildFacility(),
                buildCondition("condition-1"),
                buildDevice("XYZ-123"),
                buildProcedure("12345678"),
                buildMedication(40) );
        outParams = crdop.coverageRequirementsDiscovery(request, endpoint, requestQualification);
        assertTrue(outParams.isEmpty());

        // missing insurer
        request = buildRequest(
                buildEligibilityRequest("1234"),
                buildPatient("patient-4", "Bob Smith"),
                buildCoverage("4321"),
                buildProvider("5678"),
                null,
                buildFacility(),
                buildCondition("condition-1"),
                buildDevice("XYZ-123"),
                buildProcedure("12345678"),
                buildMedication(40) );
        outParams = crdop.coverageRequirementsDiscovery(request, endpoint, requestQualification);
        assertTrue(outParams.isEmpty());
    }


    private EligibilityRequest buildEligibilityRequest(String id) {
        // create an EligibilityRequest object with ID set
        EligibilityRequest eligibilityRequest = new EligibilityRequest();
        if (!id.equals("")) {
            eligibilityRequest.setId(id); // "1234"
        }
        return eligibilityRequest;
    }

    private Patient buildPatient(String id, String nameStr) {
        // create a Patient object with Name set
        Patient patient = new Patient();
        if (!id.equals("")) {
            patient.setId(id); // "patient-4"
        }
        if (!nameStr.equals("")) {
            ArrayList<HumanName> names = new ArrayList<HumanName>();
            HumanName name = new HumanName();
            name.setText(nameStr); // "Bob Smith"
            names.add(name);
            patient.setName(names);
        }
        return patient;
    }

    private Coverage buildCoverage(String id) {
        // create a Coverage object with ID set
        Coverage coverage = new Coverage();
        if (!id.equals("")) {
            coverage.setId(id); // "4321"
        }
        return coverage;
    }

    private Practitioner buildProvider(String id) {
        // create a Practitioner object with ID set
        Practitioner provider = new Practitioner();
        if (!id.equals("")) {
            provider.setId(id); // "5678"
        }
        return provider;
    }

    private Organization buildInsurer(String id, String name) {
        // create an Organization object with ID and Name set
        Organization insurer = new Organization();
        if (!id.equals("")) {
            insurer.setId(id); // "876545"
        }
        if (!name.equals("")) {
            insurer.setName(name); // "InsureCo"
        }
        return insurer;
    }

    private Location buildFacility() {
        // create a Location Object
        Location facility = new Location();
        return facility;
    }


    private Condition buildCondition(String id) {
        // create a Condition for the patientContext
        Condition condition = new Condition();
        if (!id.equals("")) {
            condition.setId(id); // "condition-1"
        }
        return condition;
    }

    private Device buildDevice(String model) {
        // create a Device for the patientContext
        Device device = new Device();
        if (!model.equals("")) {
            device.setModel(model); // "XYZ-123"
        }
        return device;
    }

    private Procedure buildProcedure(String id) {
        // create a Procedure for the serviceInformationReference
        Procedure procedure = new Procedure();
        if (!id.equals("")) {
            procedure.setId(id); // "12345678"
        }
        return procedure;
    }

    private Medication buildMedication(long quantity) {
        // create a Medication for the serviceInformationReference
        Medication medication = new Medication();
        if (quantity != 0) {
            SimpleQuantity simpleQuantity = new SimpleQuantity();
            simpleQuantity.setValue(quantity); // 40
            medication.setAmount(simpleQuantity);
        }
        return medication;
    }

    private Parameters.ParametersParameterComponent buildRequest(
            EligibilityRequest eligibilityRequest,
            Patient patient,
            Coverage coverage,
            Practitioner provider,
            Organization insurer,
            Location facility,
            Condition condition,
            Device device,
            Procedure procedure,
            Medication medication) {
        Parameters.ParametersParameterComponent request = new Parameters.ParametersParameterComponent();
        request.setName("request");

        request.addPart().setName("eligibilityrequest").setResource(eligibilityRequest);
        request.addPart().setName("patient").setResource(patient);
        request.addPart().setName("coverage").setResource(coverage);
        request.addPart().setName("provider").setResource(provider);
        request.addPart().setName("insurer").setResource(insurer);
        request.addPart().setName("facility").setResource(facility);
        request.addPart().setName("patientContext").setResource(condition);
        request.addPart().setName("patientContext").setResource(device);
        request.addPart().setName("serviceInformationReference").setResource(procedure);
        request.addPart().setName("serviceInformationReference").setResource(medication);

        return request;
    }

    private Endpoint buildEndpoint() {
        // create an Endpoint object
        Endpoint endpoint = new Endpoint();
        return endpoint;
    }

    private CodeableConcept buildRequestQualification() {
        // create a CodeableConcept requestQualifiction object
        CodeableConcept requestQualification = new CodeableConcept();
        return requestQualification;
    }
}