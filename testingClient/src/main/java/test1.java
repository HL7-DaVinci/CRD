import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.davinci.DaVinciEligibilityResponse;
import org.hl7.davinci.DaVinciPatient;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;


public class test1 {

    public static void main(String[] args) {

        // Create a client to talk to the server
        FhirContext ctx = FhirContext.forR4();
        IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/server/fhir");
        client.registerInterceptor(new LoggingInterceptor(true));



        //make new davinci patient
        DaVinciPatient pt = new DaVinciPatient();
        pt.setFavoriteColor(new StringType("blue"));
        pt.setLanguage("english");

        // Create the input parameters to pass to the server
        Parameters inParams = new Parameters();
        inParams.addParameter().setName("patient").setResource(pt);

        // Invoke operation
        DaVinciEligibilityResponse eligibilityResponse = client
                .operation()
                .onServer()
                .named("$coverage-requirement-discovery")
                .withParameters(inParams)
                .returnResourceType(DaVinciEligibilityResponse.class)
                .execute();

        System.out.println(eligibilityResponse.getDisposition());
    }
}
