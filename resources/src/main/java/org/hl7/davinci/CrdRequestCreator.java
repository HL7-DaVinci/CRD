package org.hl7.davinci;

import java.util.Date;
import java.util.UUID;

import org.hl7.davinci.cdshooks.Hook;
import org.hl7.davinci.cdshooks.orderreview.OrderReviewRequest;
import org.hl7.davinci.cdshooks.orderreview.OrderReviewContext;
import org.hl7.davinci.cdshooks.CrdPrefetch;
import org.hl7.fhir.r4.model.*;


/**
 * CrdRequestCreator is a class that creates example CRD requests in the form of a CDS Hook.
 */
public class CrdRequestCreator {

  /**
   * Generate a request.
   *
   * @param patientGender    Desired gender of the patient in the request
   * @param patientBirthdate Desired birth date of the patient in the request
   * @return Fully populated CdsRequest
   */
  public static OrderReviewRequest createRequest(Enumerations.AdministrativeGender patientGender,
                                                 Date patientBirthdate) {

    OrderReviewRequest request = new OrderReviewRequest();
    request.setUser("Practitioner/1234");
    request.setHook(Hook.ORDER_REVIEW);
    request.setHookInstance(UUID.randomUUID());
    OrderReviewContext context = new OrderReviewContext();
    CrdPrefetch prefetch = new CrdPrefetch();
    request.setContext(context);
//    request.setPrefetch(prefetch);

    DeviceRequest dr = new DeviceRequest();
    dr.setStatus(DeviceRequest.DeviceRequestStatus.DRAFT);
    dr.setId("DeviceRequest/123");

    Coding oxygen = new Coding().setCode("E0424").setSystem("https://bluebutton.cms.gov/resources/codesystem/hcpcs")
            .setDisplay("Stationary Compressed Gaseous Oxygen System, Rental");
    dr.setCode(new CodeableConcept().addCoding(oxygen));
    Bundle orderBundle = new Bundle();
    Bundle.BundleEntryComponent bec = new Bundle.BundleEntryComponent();
    bec.setResource(dr);
    orderBundle.addEntry(bec);
    context.setOrders(orderBundle);

    // create a Patient object with Name set
    Patient patient = new Patient();
    patient.setId(idString());
    patient.setGender(patientGender);
    patient.setBirthDate(patientBirthdate);
    context.setPatientId(patient.getId());
    prefetch.setPatient(patient);
    dr.setSubject(generateReference(ResourceType.Patient, patient));

    // create a Practitioner object with ID set
    Practitioner provider = new Practitioner();
    provider.setId(idString());
    provider.addIdentifier(new Identifier().setSystem("http://hl7.org/fhir/sid/us-npi").setValue("1122334455"));
    provider.addName(new HumanName().addGiven("Jane").setFamily("Doe").addPrefix("Dr."));


    // create an Organization object with ID and Name set
    Organization insurer = new Organization();
    insurer.setId(idString());
    insurer.setName("Centers for Medicare and Medicaid Services");
    prefetch.setInsurer(insurer);

    // create a Location Object
    Location facility = new Location();
    facility.setId(idString());
    facility.setAddress(new Address().addLine("100 Good St")
        .setCity("Bedford")
        .setState("MA")
        .setPostalCode("01730"));
    prefetch.setLocation(facility);

    Device device = new Device();
    device.setType(new CodeableConcept().addCoding(oxygen));
    prefetch.setDevice(device);

    PractitionerRole pr = new PractitionerRole();
    pr.setId(idString());
    pr.setPractitioner(generateReference(ResourceType.Practitioner, provider));
    pr.addLocation(generateReference(ResourceType.Location, facility));
    prefetch.setPractitionerRole(pr);
    dr.setPerformer(generateReference(ResourceType.PractitionerRole, pr));
    prefetch.setPractitionerRole(pr);

    // create a Coverage object with ID set
    Coverage coverage = new Coverage();
    coverage.setId(idString());
    Coding planCode = new Coding().setCode("plan").setSystem("http://hl7.org/fhir/coverage-class");
    Coverage.ClassComponent coverageClass = new Coverage.ClassComponent();
    coverageClass.setType(planCode).setValue("Medicare Part D");
    coverage.addClass_(coverageClass);
    coverage.addPayor(generateReference(ResourceType.Organization, insurer));
    dr.addInsurance(generateReference(ResourceType.Coverage, coverage));
    prefetch.setCoverage(coverage);

    return request;
  }

  private static String idString() {
    UUID uuid = UUID.randomUUID();
    return uuid.toString();
  }

  private static Reference generateReference(ResourceType type, Resource resource) {
    Reference reference = new Reference();
    return reference.setReference(String.format("%s/%s", type.toString(), resource.getId()));
  }
}
