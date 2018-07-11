import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.davinci.DaVinciEligibilityResponse;
import org.hl7.davinci.DaVinciPatient;
import org.hl7.fhir.r4.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class test1 {
    final static Logger logger = LoggerFactory.getLogger(test1.class);

    public static void main(String[] args) {
        // Create a client to talk to the server
        FhirContext ctx = FhirContext.forR4();
        IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/server/fhir");
        client.registerInterceptor(new LoggingInterceptor(true));

        runTestOperation(client);
        runCRD(client);
    }

    public static void runTestOperation(IGenericClient client) {
        //make new davinci patient
        DaVinciPatient pt = new DaVinciPatient();
        pt.setFavoriteColor(new StringType("blue"));
        pt.setLanguage("english");

        // Create the input parameters to pass to the server
        Parameters inParams = new Parameters();
        inParams.addParameter().setName("patient").setResource(pt);

        // Invoke operation
        DaVinciEligibilityResponse eligibilityResponseT = client
                .operation()
                .onServer()
                .named("$operation-test")
                .withParameters(inParams)
                .returnResourceType(DaVinciEligibilityResponse.class)
                .execute();

        logger.debug(eligibilityResponseT.getDisposition());
    }

    public static Parameters buildParams() {
        // build the parameters for the CRD
        Parameters crdParams = new Parameters();

        // create an EligibilityRequest object with ID set
        EligibilityRequest eligibilityRequest = new EligibilityRequest();
        eligibilityRequest.setId("1234");

        // create a Patient object with Name set
        Patient patient = new Patient();
        patient.setId("patient-4");
        ArrayList<HumanName> names = new ArrayList<HumanName>();
        HumanName name = new HumanName();
        name.setText("Bob Smith");
        names.add(name);
        patient.setName(names);

        // create a Coverage object with ID set
        Coverage coverage = new Coverage();
        coverage.setId("4321");

        // create a Practitioner object with ID set
        Practitioner provider = new Practitioner();
        provider.setId("5678");

        // create an Organization object with ID and Name set
        Organization insurer = new Organization();
        insurer.setId("87654");
        insurer.setName("InsureCo");

        // create a Location Object
        Location facility = new Location();


        // create a Condition for the patientContext
        Condition condition = new Condition();
        condition.setId("condition-1");

        // create a Device for the patientContext
        Device device = new Device();
        device.setModel("XYZ-123");

        // create a Procedure for the serviceInformationReference
        Procedure procedure = new Procedure();
        procedure.setId("12345678");

        // create a Medication for the serviceInformationReference
        Medication medication = new Medication();
        SimpleQuantity simpleQuantity = new SimpleQuantity();
        simpleQuantity.setValue(40);
        medication.setAmount(simpleQuantity);

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
        param.addPart().setName("patientContext").setResource(device);
        param.addPart().setName("serviceInformationReference").setResource(procedure);
        param.addPart().setName("serviceInformationReference").setResource(medication);

        // create and add an Endpoint object to the CRD parameters
        Endpoint endpoint = new Endpoint();
        crdParams.addParameter().setName("endpoint").setResource(endpoint);

        // create and add an CodeableConcpt object to the CRD parameters
        CodeableConcept requestQualification = new CodeableConcept();
        crdParams.addParameter().setName("requestQualification").setValue(requestQualification);

        printResource(crdParams);

        return crdParams;
    }

    public static void runCRD(IGenericClient client) {
        // build the parameters for the CRD
        Parameters crdParams = buildParams();

        // call the CRD operation
        Parameters retParams = client.operation()
                .onServer()
                .named("$coverage-requirements-discovery")
                .withParameters(crdParams)
                .returnResourceType(Parameters.class)
                .execute();

        // make sure the return parameters are valid
        if (retParams == null) {
            logger.error("ERROR: retParams is null");
            return;
        }

        // parse the return parameters to get each object
        EligibilityResponse eligibilityResponse = null;
        Practitioner returnProvider = null;
        EligibilityRequest returnEligibilityRequest = null;
        Organization returnInsurer = null;
        Coverage returnCoverage = null;
        Endpoint returnEndpoint = null;

        printResource(retParams);

        List<Parameters.ParametersParameterComponent> paramList = retParams.getParameter().get(0).getPart();
        for (Parameters.ParametersParameterComponent part : paramList) {
            switch (part.getName()) {
                case "eligibilityResponse":
                    eligibilityResponse = (EligibilityResponse) part.getResource();
                    logger.debug("CRD: got response.eligibilityResponse");
                    break;
                case "requestProvider":
                    returnProvider = (Practitioner) part.getResource();
                    logger.debug("CRD: got response.requestProvider");
                    break;
                case "request":
                    returnEligibilityRequest = (EligibilityRequest) part.getResource();
                    logger.debug("CRD: got response.request");
                    break;
                case "insurer":
                    returnInsurer = (Organization) part.getResource();
                    logger.debug("CRD: got response.insurer");
                    break;
                case "coverage":
                    returnCoverage = (Coverage) part.getResource();
                    logger.debug("CRD: got response.coverage");
                    break;
                case "endPoint":
                    returnEndpoint = (Endpoint) part.getResource();
                    logger.debug("CRD: got response.endpoint");
                    break;
                case "service":
                    ResourceType serviceType = part.getResource().getResourceType();
                    switch (serviceType) {
                        case Procedure:
                            logger.debug("CRD: got response.service of type Procedure");
                            break;
                        case HealthcareService:
                            logger.debug("CRD: got response.service of type HealthcareService");
                            break;
                        case ServiceRequest:
                            logger.debug("CRD: got response.service of type ServiceRequest");
                            break;
                        case MedicationRequest:
                            logger.debug("CRD: got response.service of type MedicationRequest");
                            break;
                        case Medication:
                            logger.debug("CRD: got response.service of type Medication");
                            break;
                        case Device:
                            logger.debug("CRD: got response.service of type Device");
                            break;
                        case DeviceRequest:
                            logger.debug("CRD: got response.service of type DeviceRequest");
                            break;
                        default:
                            logger.debug("Warning: unexpected response.service type");
                            break;
                    }
                    break;
                default:
                    logger.warn("Warning: unexpected parameter part: " + part.getName());
                    break;
            }
        }

        logger.debug("returned from CRD call!");
        if (eligibilityResponse != null) {
            logger.debug("CRD Disposition: " + eligibilityResponse.getDisposition());
        } else {
            logger.error("ERROR: eligibilityResponse is null");
        }
    }

    static void printResource(Resource obj) {
        FhirContext ctx = FhirContext.forR4();
        String encoded = ctx.newXmlParser().encodeResourceToString(obj);
        logger.debug("\n" + encoded + "\n");
    }
}