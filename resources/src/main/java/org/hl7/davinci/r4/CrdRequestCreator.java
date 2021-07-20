package org.hl7.davinci.r4;

import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import org.cdshooks.Hook;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.orderselect.OrderSelectContext;
import org.hl7.davinci.r4.crdhook.orderselect.OrderSelectRequest;
import org.hl7.davinci.r4.crdhook.ordersign.OrderSignContext;
import org.hl7.davinci.r4.crdhook.ordersign.OrderSignRequest;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Address.AddressType;
import org.hl7.fhir.r4.model.Address.AddressUse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * CrdRequestCreator is a class that creates example CRD requests in the form of a CDS Hook.
 */
public class CrdRequestCreator {
  static final Logger logger = LoggerFactory.getLogger(CrdRequestCreator.class);

  /**
   * Generate a order select request that contains a DeviceRequest.
   *
   * @param patientGender Desired gender of the patient in the request
   * @param patientBirthdate Desired birth date of the patient in the request
   * @return Fully populated CdsRequest
   */
  public static OrderSelectRequest createOrderSelectRequest(
      Enumerations.AdministrativeGender patientGender,
      Date patientBirthdate, String patientAddressState, String providerAddressState,
      Coding requestCoding, Coding statementCoding) {

    OrderSelectRequest request = new OrderSelectRequest();
    request.setHook(Hook.ORDER_SELECT);
    request.setHookInstance(UUID.randomUUID().toString());
    OrderSelectContext context = new OrderSelectContext();
    request.setContext(context);
    context.setUserId("Practitioner/1234");
    Patient patient = createPatient(patientGender, patientBirthdate, patientAddressState);
    context.setPatientId(patient.getId());

    // create and build MedicationRequest
    MedicationRequest mr = new MedicationRequest();
    mr.setStatus(MedicationRequest.MedicationRequestStatus.DRAFT);
    mr.setId("MedicationRequest/123");
    mr.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);

    PrefetchCallback callback = (p, c) -> {
      mr.setPerformer(new Reference(p));
      mr.addInsurance(new Reference(c));
    };
    mr.setSubject(new Reference(patient));
    Practitioner provider = createPractitioner();
    Bundle prefetchBundle = createPrefetchBundle(patient, provider, callback, providerAddressState);


    mr.setMedication(new CodeableConcept().addCoding(requestCoding));
    Bundle orderBundle = new Bundle();
    Bundle.BundleEntryComponent bec = new Bundle.BundleEntryComponent();
    bec.setResource(mr);
    orderBundle.addEntry(bec);
    Bundle.BundleEntryComponent pfDrBec = new Bundle.BundleEntryComponent();
    pfDrBec.setResource(mr);
    prefetchBundle.addEntry(pfDrBec);
    context.setDraftOrders(orderBundle);
    context.setSelections(new String[] {"123"});


    Bundle prefetchMedicationStatementBundle = new Bundle();
    bec = new Bundle.BundleEntryComponent();
    bec.setResource(patient);
    prefetchMedicationStatementBundle.addEntry(bec);
    bec = new Bundle.BundleEntryComponent();
    MedicationStatement ms = new MedicationStatement();
    ms.setId("MedciationStatement/12345");
    ms.setMedication(new CodeableConcept().addCoding(statementCoding));
    bec.setResource(ms);
    prefetchMedicationStatementBundle.addEntry(bec);


    // add the prefetch into the request
    CrdPrefetch prefetch = new CrdPrefetch();
    prefetch.setMedicationRequestBundle(prefetchBundle);
    prefetch.setMedicationStatementBundle(prefetchMedicationStatementBundle);
    request.setPrefetch(prefetch);

    return request;
  }

  /**
   * Generate a order sign request that contains a ServiceRequest.
   *
   * @param patientGender Desired gender of the patient in the request
   * @param patientBirthdate Desired birth date of the patient in the request
   * @return Fully populated CdsRequest
   */
  public static OrderSignRequest createOrderSignRequest(
      Enumerations.AdministrativeGender patientGender,
      Date patientBirthdate, String patientAddressState, String providerAddressState) {

    OrderSignRequest request = new OrderSignRequest();
    request.setHook(Hook.ORDER_SIGN);
    request.setHookInstance(UUID.randomUUID().toString());
    OrderSignContext context = new OrderSignContext();
    request.setContext(context);
    context.setUserId("Practitioner/1234");
    Patient patient = createPatient(patientGender, patientBirthdate, patientAddressState);
    context.setPatientId(patient.getId());

    ServiceRequest sr = new ServiceRequest();
    sr.setStatus(ServiceRequest.ServiceRequestStatus.DRAFT);
    sr.setId("DeviceRequest/123");
    sr.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);

    PrefetchCallback callback = (p, c) -> {
      sr.addPerformer(new Reference(p));
      sr.addInsurance(new Reference(c));
    };
    sr.setSubject(new Reference(patient));
    Practitioner provider = createPractitioner();
    Bundle prefetchBundle = createPrefetchBundle(patient, provider, callback, providerAddressState);

    Coding oxygen = new Coding().setCode("A0426")
        .setSystem("https://bluebutton.cms.gov/resources/codesystem/hcpcs")
        .setDisplay("Ambulance service, advanced life support, non-emergency transport, level 1 (als 1)");
    sr.setCode(new CodeableConcept().addCoding(oxygen).setText("Ambulance service Non-Emergency Transport"));
    Bundle orderBundle = new Bundle();
    Bundle.BundleEntryComponent bec = new Bundle.BundleEntryComponent();
    bec.setResource(sr);
    orderBundle.addEntry(bec);
    Bundle.BundleEntryComponent pfDrBec = new Bundle.BundleEntryComponent();
    pfDrBec.setResource(sr);
    prefetchBundle.addEntry(pfDrBec);
    context.setDraftOrders(orderBundle);

    Device device = new Device();
    device.setType(new CodeableConcept().addCoding(oxygen));
    bec = new Bundle.BundleEntryComponent();
    bec.setResource(device);
    prefetchBundle.addEntry(bec);

    CrdPrefetch prefetch = new CrdPrefetch();
    prefetch.setServiceRequestBundle(prefetchBundle);
    request.setPrefetch(prefetch);

    return request;
  }

  private static Bundle createPrefetchBundle(Patient patient, Practitioner provider,
      PrefetchCallback cb, String providerAddressState) {
    Bundle prefetchBundle = new Bundle();

    Bundle.BundleEntryComponent bec = new Bundle.BundleEntryComponent();
    bec.setResource(patient);
    prefetchBundle.addEntry(bec);

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
        .setState(providerAddressState)
        .setPostalCode("01730"));
    bec = new Bundle.BundleEntryComponent();
    bec.setResource(facility);
    prefetchBundle.addEntry(bec);

    PractitionerRole pr = new PractitionerRole();
    pr.setId(idString());
    pr.setPractitioner(new Reference(provider));
    pr.addLocation(new Reference(facility));

    bec = new Bundle.BundleEntryComponent();
    bec.setResource(pr);
    prefetchBundle.addEntry(bec);

    // create a Coverage object with ID set
    Coverage coverage = new Coverage();
    coverage.setId(idString());
    Coding planCode = new Coding().setCode("plan").setSystem("http://hl7.org/fhir/coverage-class");
    CodeableConcept codeableConcept = new CodeableConcept();
    List<Coding> codingList = new ArrayList<Coding>();
    codingList.add(planCode);
    codeableConcept.setCoding(codingList);
    Coverage.ClassComponent coverageClass = new Coverage.ClassComponent();
    coverageClass.setType(codeableConcept).setValue("Medicare Part D");
    coverage.addClass_(coverageClass);
    coverage.addPayor(new Reference(insurer));
    bec = new Bundle.BundleEntryComponent();
    bec.setResource(coverage);
    prefetchBundle.addEntry(bec);
    cb.callback(pr, coverage);

    return prefetchBundle;
  }

  private static Patient createPatient(Enumerations.AdministrativeGender patientGender,
      Date patientBirthdate, String patientAddressState) {
    Patient patient = new Patient();
    patient.setId(idString());
    patient.setGender(patientGender);
    patient.setBirthDate(patientBirthdate);

    Address address = new Address();
    address.setUse(AddressUse.HOME);
    address.setType(AddressType.BOTH);
    address.setState(patientAddressState);
    patient.addAddress(address);

    return patient;
  }

  private static Practitioner createPractitioner() {
    // create a Practitioner object with ID set
    Practitioner provider = new Practitioner();
    provider.setId(idString());
    provider.addIdentifier(
        new Identifier().setSystem("http://hl7.org/fhir/sid/us-npi").setValue("1122334455"));
    provider.addName(new HumanName().addGiven("Jane").setFamily("Doe").addPrefix("Dr."));
    return provider;
  }

  private static String idString() {
    UUID uuid = UUID.randomUUID();
    return uuid.toString();
  }

  interface PrefetchCallback {

    void callback(PractitionerRole provider, Coverage coverage);
  }
}
