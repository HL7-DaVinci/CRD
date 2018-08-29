package org.hl7.davinci.creator;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.dstu3.model.Patient;


public class blahblah {
  public static void main(String[] args) throws Exception {
    String serverBase = "http://localhost:8080/hapi-fhir-jpaserver-example/baseDstu3/";
    FhirContext ctx = FhirContext.forDstu3();
    IGenericClient client = ctx.newRestfulGenericClient(serverBase);
    Patient patient = new Patient();
    patient.addIdentifier().setSystem("urn:system").setValue("12345");
    patient.addName().addGiven("John");
//
    MethodOutcome outcome = client.create()
        .resource(patient)
        .prettyPrint()
        .execute();
//
    IdDt id = (IdDt) outcome.getId();
    System.out.println("Got ID: " + id.getValue());
  }
}
