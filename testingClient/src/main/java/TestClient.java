import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.davinci.CRDRequestCreator;
import org.hl7.davinci.DaVinciEligibilityResponse;
import org.hl7.davinci.DaVinciPatient;
import org.hl7.fhir.r4.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
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


    public static void runCRD(IGenericClient client) {
        // build the parameters for the CRD
        Calendar cal = Calendar.getInstance();
        cal.set(1970, Calendar.JULY, 4);
        Parameters crdParams = CRDRequestCreator.createRequest(Enumerations.AdministrativeGender.MALE, cal.getTime());

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