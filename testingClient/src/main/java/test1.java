import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.davinci.DaVinciEligibilityResponse;
import org.hl7.davinci.DaVinciPatient;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;


public class test1 {

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

        System.out.println(eligibilityResponseT.getDisposition());
    }

    public static void runCRD(IGenericClient client) {
        // build the parameters for the CRD
        Parameters crdParams = new Parameters();

        // build the request parameters
        Parameters requestParams = new Parameters();

        // create an EligibilityRequest object with ID set
        EligibilityRequest eligibilityRequest = new EligibilityRequest();
        eligibilityRequest.setId("1234");
        requestParams.addParameter().setName("eligibilityrequest").setResource(eligibilityRequest);

        // create a Patient object with Name set
        Patient patient = new Patient();
        ArrayList<HumanName> names = new ArrayList<HumanName>();
        HumanName name = new HumanName();
        name.setText("Bob Smith");
        names.add(name);
        patient.setName(names);
        requestParams.addParameter().setName("patient").setResource(patient);

        // create a Coverage object with ID set
        Coverage coverage = new Coverage();
        coverage.setId("4321");
        requestParams.addParameter().setName("coverage").setResource(coverage);

        // create a Practitioner object with ID set
        Practitioner provider = new Practitioner();
        provider.setId("5678");
        requestParams.addParameter().setName("provider").setResource(provider);

        // create an Organization object with ID and Name set
        Organization insurer = new Organization();
        insurer.setId("87654");
        insurer.setName("InsureCo");
        requestParams.addParameter().setName("insurer").setResource(insurer);

        // create a Location Object
        Location facility = new Location();
        requestParams.addParameter().setName("facility").setResource(facility);

        // add the request to the CRD parameters
        crdParams.addParameter().setName("request").setResource(requestParams);

        // create and add an Endpoint object to the CRD parameters
        Endpoint endpoint = new Endpoint();
        crdParams.addParameter().setName("endpoint").setResource(endpoint);

        // create and add an CodeableConcpt object to the CRD parameters
        CodeableConcept requestQualification = new CodeableConcept();
        crdParams.addParameter().setName("requestQualification").setValue(requestQualification);

        // call the CRD operation
        Parameters retParams = client.operation()
                .onServer()
                .named("$coverage-requirements-discovery")
                .withParameters(crdParams)
                .returnResourceType(Parameters.class)
                .execute();

        // make sure the retuned paramets are valid
        if (retParams == null) {
            System.out.println("ERROR: retParams is null");
            return;
        }

        // parse the return parameters to get each object
        EligibilityResponse eligibilityResponse = null;
        Practitioner returnProvider = null;
        EligibilityRequest returnEligibilityRequest = null;
        Organization returnInsurer = null;
        Coverage returnCoverage = null;
        Endpoint returnEndpoint = null;


        List<Parameters.ParametersParameterComponent> paramList = retParams.getParameter();
        for (Parameters.ParametersParameterComponent part : paramList) {
            switch (part.getName()) {
                case "eligibilityResponse":
                    eligibilityResponse = (EligibilityResponse) part.getResource();
                    break;
                case "provider":
                    returnProvider = (Practitioner) part.getResource();
                    break;
                case "request":
                    returnEligibilityRequest = (EligibilityRequest) part.getResource();
                    break;
                case "insurer":
                    returnInsurer = (Organization) part.getResource();
                    break;
                case "coverage":
                    returnCoverage = (Coverage) part.getResource();
                    break;
                case "endpoint":
                    returnEndpoint = (Endpoint) part.getResource();
                    break;
                    /* TODO zzzz handle 0..* of these...
                case "service":
                    break;
                    zzzz */
                default:
                    break;
            }
        }

        System.out.println("returned from CRD call!");
        if (eligibilityResponse != null) {
            System.out.println("CRD Disposition: " + eligibilityResponse.getDisposition());
        } else {
            System.out.println("ERROR: eligibilityResponse is null");
        }
    }
}