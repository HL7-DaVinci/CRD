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
    Bundle prefetchBundle = new Bundle();
    prefetch.setDeviceRequestBundle(prefetchBundle);
    request.setPrefetch(prefetch);

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
    Bundle.BundleEntryComponent pfDrBec = new Bundle.BundleEntryComponent();
    pfDrBec.setResource(dr);
    prefetchBundle.addEntry(pfDrBec);
    context.setOrders(orderBundle);

    // create a Patient object with Name set
    Patient patient = new Patient();
    patient.setId(idString());
    patient.setGender(patientGender);
    patient.setBirthDate(patientBirthdate);
    context.setPatientId(patient.getId());
    bec = new Bundle.BundleEntryComponent();
    bec.setResource(patient);
    prefetchBundle.addEntry(bec);
    dr.setSubject(new Reference(patient));

    // create a Practitioner object with ID set
    Practitioner provider = new Practitioner();
    provider.setId(idString());
    provider.addIdentifier(new Identifier().setSystem("http://hl7.org/fhir/sid/us-npi").setValue("1122334455"));
    provider.addName(new HumanName().addGiven("Jane").setFamily("Doe").addPrefix("Dr."));
    bec = new Bundle.BundleEntryComponent();
    bec.setResource(provider);
    prefetchBundle.addEntry(bec);

    // create an Organization object with ID and Name set
    Organization insurer = new Organization();
    insurer.setId(idString());
    insurer.setName("Centers for Medicare and Medicaid Services");
    bec = new Bundle.BundleEntryComponent();
    bec.setResource(insurer);
    prefetchBundle.addEntry(bec);

    // create a Location Object
    Location facility = new Location();
    facility.setId(idString());
    facility.setAddress(new Address().addLine("100 Good St")
        .setCity("Bedford")
        .setState("MA")
        .setPostalCode("01730"));
    bec = new Bundle.BundleEntryComponent();
    bec.setResource(facility);
    //prefetchBundle.addEntry(bec);

    Device device = new Device();
    device.setType(new CodeableConcept().addCoding(oxygen));
    bec = new Bundle.BundleEntryComponent();
    bec.setResource(device);
    prefetchBundle.addEntry(bec);

    PractitionerRole pr = new PractitionerRole();
    pr.setId(idString());
    pr.setPractitioner(new Reference(provider));
    pr.addLocation(new Reference(facility));
    dr.setPerformer(new Reference(pr));
    bec = new Bundle.BundleEntryComponent();
    bec.setResource(pr);
    prefetchBundle.addEntry(bec);

    // create a Coverage object with ID set
    Coverage coverage = new Coverage();
    coverage.setId(idString());
    Coding planCode = new Coding().setCode("plan").setSystem("http://hl7.org/fhir/coverage-class");
    Coverage.ClassComponent coverageClass = new Coverage.ClassComponent();
    coverageClass.setType(planCode).setValue("Medicare Part D");
    coverage.addClass_(coverageClass);
    coverage.addPayor(new Reference(insurer));
    dr.addInsurance(new Reference(coverage));
    bec = new Bundle.BundleEntryComponent();
    bec.setResource(coverage);
    prefetchBundle.addEntry(bec);

    return request;
  }

  private static String idString() {
    UUID uuid = UUID.randomUUID();
    return uuid.toString();
  }
}
