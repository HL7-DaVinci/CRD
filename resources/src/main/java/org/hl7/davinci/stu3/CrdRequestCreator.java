package org.hl7.davinci.stu3;

import java.util.Date;
import java.util.UUID;
import org.cdshooks.Hook;
import org.hl7.davinci.stu3.crdhook.CrdPrefetch;
import org.hl7.davinci.stu3.crdhook.medicationprescribe.MedicationPrescribeContext;
import org.hl7.davinci.stu3.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.davinci.stu3.crdhook.orderreview.OrderReviewContext;
import org.hl7.davinci.stu3.crdhook.orderreview.OrderReviewRequest;
import org.hl7.davinci.stu3.fhirresources.DaVinciDeviceRequest;
import org.hl7.davinci.stu3.fhirresources.DaVinciMedicationRequest;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Address.AddressType;
import org.hl7.fhir.dstu3.model.Address.AddressUse;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Coverage;
import org.hl7.fhir.dstu3.model.Coverage.GroupComponent;
import org.hl7.fhir.dstu3.model.Device;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestRequesterComponent;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Reference;


/**
 * CrdRequestCreator is a class that creates example CRD requests in the form of a CDS Hook.
 */
public class CrdRequestCreator {

  /**
   * Generate a request.
   *
   * @param patientGender Desired gender of the patient in the request
   * @param patientBirthdate Desired birth date of the patient in the request
   * @return Fully populated CdsRequest
   */
  public static OrderReviewRequest createOrderReviewRequest(
      Enumerations.AdministrativeGender patientGender,
      Date patientBirthdate, String patientAddressState, String providerAddressState) {

    OrderReviewRequest request = new OrderReviewRequest();
    request.setUser("Practitioner/1234");
    request.setHook(Hook.ORDER_REVIEW);
    request.setHookInstance(UUID.randomUUID());
    OrderReviewContext context = new OrderReviewContext();
    request.setContext(context);
    Patient patient = createPatient(patientGender, patientBirthdate, patientAddressState);
    context.setPatientId(patient.getId());

    DaVinciDeviceRequest deviceRequest = new DaVinciDeviceRequest();
    deviceRequest.setStatus(DaVinciDeviceRequest.DeviceRequestStatus.DRAFT);
    deviceRequest.setId("DeviceRequest/123");

    PrefetchCallback callback = (p, c) -> {
      deviceRequest.setPerformer(new Reference(p));
      deviceRequest.addInsurance(new Reference(c));
    };
    deviceRequest.setSubject(new Reference(patient));
    Practitioner provider = createPractitioner();
    Bundle prefetchBundle = createPrefetchBundle(patient, provider, callback, providerAddressState);
    CrdPrefetch prefetch = new CrdPrefetch();
    prefetch.setDeviceRequestBundle(prefetchBundle);
    request.setPrefetch(prefetch);

    Coding oxygen = new Coding().setCode("E0424")
        .setSystem("https://bluebutton.cms.gov/resources/codesystem/hcpcs")
        .setDisplay("Stationary Compressed Gaseous Oxygen System, Rental");
    deviceRequest.setCode(new CodeableConcept().addCoding(oxygen));
    Bundle orderBundle = new Bundle();
    Bundle.BundleEntryComponent bec = new Bundle.BundleEntryComponent();
    bec.setResource(deviceRequest);
    orderBundle.addEntry(bec);
    Bundle.BundleEntryComponent pfDrBec = new Bundle.BundleEntryComponent();
    pfDrBec.setResource(deviceRequest);
    prefetchBundle.addEntry(pfDrBec);
    context.setOrders(orderBundle);

    Device device = new Device();
    device.setType(new CodeableConcept().addCoding(oxygen));
    bec = new Bundle.BundleEntryComponent();
    bec.setResource(device);
    prefetchBundle.addEntry(bec);

    return request;
  }

  /**
   * Generate a request.
   *
   * @param patientGender Desired gender of the patient in the request
   * @param patientBirthdate Desired birth date of the patient in the request
   * @return Fully populated CdsRequest
   */
  public static MedicationPrescribeRequest createMedicationPrescribeRequest(
      Enumerations.AdministrativeGender patientGender,
      Date patientBirthdate, String patientAddressState, String providerAddressState) {
    MedicationPrescribeRequest request = new MedicationPrescribeRequest();
    request.setUser("Practitioner/1234");
    request.setHook(Hook.MEDICATION_PRESCRIBE);
    request.setHookInstance(UUID.randomUUID());
    MedicationPrescribeContext context = new MedicationPrescribeContext();
    request.setContext(context);
    Patient patient = createPatient(patientGender, patientBirthdate, patientAddressState);
    context.setPatientId(patient.getId());

    DaVinciMedicationRequest medicationRequest = new DaVinciMedicationRequest();
    medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.DRAFT);
    medicationRequest.setId("MedicationRequest/123");

    PrefetchCallback callback = (p, c) -> {
      MedicationRequestRequesterComponent medicationRequestRequesterComponent =
          new MedicationRequestRequesterComponent();
      medicationRequestRequesterComponent.setAgent(new Reference(p));
      medicationRequest.setRequester(medicationRequestRequesterComponent);
      medicationRequest.addInsurance(new Reference(c));
    };
    medicationRequest.setSubject(new Reference(patient));
    Practitioner provider = createPractitioner();
    Bundle prefetchBundle = createPrefetchBundle(patient, provider, callback, providerAddressState);
    CrdPrefetch prefetch = new CrdPrefetch();
    prefetch.setMedicationRequestBundle(prefetchBundle);
    request.setPrefetch(prefetch);

    Coding botox = new Coding().setCode("860195")
        .setSystem("http://www.nlm.nih.gov/research/umls/rxnorm")
        .setDisplay("Botox");
    medicationRequest.setMedication(new CodeableConcept().addCoding(botox));
    Bundle medicationBundle = new Bundle();
    Bundle.BundleEntryComponent bec = new Bundle.BundleEntryComponent();
    bec.setResource(medicationRequest);
    medicationBundle.addEntry(bec);
    Bundle.BundleEntryComponent pfMrBec = new Bundle.BundleEntryComponent();
    pfMrBec.setResource(medicationRequest);
    prefetchBundle.addEntry(pfMrBec);
    context.setMedications(medicationBundle);

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
    GroupComponent groupComponent = new GroupComponent();
    groupComponent.setPlan("Medicare Part D");
    coverage.setGrouping(groupComponent);
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
