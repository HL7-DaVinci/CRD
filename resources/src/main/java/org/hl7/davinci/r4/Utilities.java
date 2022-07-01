package org.hl7.davinci.r4;

import java.util.*;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

import org.hl7.davinci.*;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Address.AddressType;
import org.hl7.fhir.r4.model.Address.AddressUse;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.ClaimResponse.AdjudicationComponent;

public class Utilities {
  /**
   * Change a fhir bundle into a hashmap keyed by resources type, where the value is a list of
   * resources of that type.
   * @return a hashmap of a ResourceType as key, and a List of Resources of that type
   */
  public static HashMap<String, List<Resource>> bundleAsHashmap(Bundle bundle) {
    HashMap<String, List<Resource>> bundleMap = new HashMap<>();
    for (BundleEntryComponent bec: bundle.getEntry()) {
      if (!bec.hasResource()) {
        continue;
      }
      Resource resource = bec.getResource();
      String resourceType = resource.getResourceType().toString();
      if (!bundleMap.containsKey(resourceType)) {
        bundleMap.put(resourceType, new ArrayList<>());
      }
      List<Resource> resourceList = (List<Resource>) bundleMap.get(resourceType);
      resourceList.add(resource);
    }
    return bundleMap;
  }

  /**
   * Gets a list of the specified type of resources from a bundle. For example, since multiple
   * device requests could be in a bundle along with other elements, this can provide a list of them.
   * @param type The class of the resources you want.
   * @param bundle The bundle that might have some the resources you want.
   * @param <T> The class of the resource you want.
   * @return A list of resources of desired type extracted from the bundle.
   */
  public static <T extends Resource> List<T> getResourcesOfTypeFromBundle(
      Class<T> type, Bundle bundle) {
    List<T> retList = new ArrayList<>();
    if (bundle == null || bundle.getEntry() == null) {
      return retList;
    }
    for (BundleEntryComponent bec: bundle.getEntry()) {
      if (!bec.hasResource()) {
        continue;
      }
      Resource resource = bec.getResource();
      if (resource.getClass() == type) {
        retList.add(type.cast(resource));
      }
    }
    return retList;
  }

  /**
   * Gets all resources that are any of multiple types.
   * @param types The classes of the resources you want.
   * @param bundle The bundle that might have some the resources you want.
   * @return A list of resources of desired type extracted from the bundle.
   */
  public List<DomainResource> getResourcesOfTypesFromBundle(
      List<Class<? extends DomainResource>> types, Bundle bundle) {
    List<DomainResource> retList = new ArrayList<>();
    for (BundleEntryComponent bec: bundle.getEntry()) {
      if (!bec.hasResource()) {
        continue;
      }
      Resource resource = bec.getResource();
      for (Class<? extends DomainResource> type:types) {
        if (resource.getClass() == type) {
          retList.add(type.cast(resource));
        }
      }

    }
    return retList;
  }


  /**
   * Returns the first match for an address in a list of addresses that is a
   * physical home.
   * @param addresses the list of addresses.
   * @return the first physical home in the list
   */
  public static Address getFirstPhysicalHomeAddress(List<Address> addresses) {
    for (Address address : addresses) {
      if (address.getUse() == AddressUse.HOME
          && (address.getType() == AddressType.BOTH
          ||  address.getType() == AddressType.PHYSICAL)) {
        return address;
      }
    }
    return null;
  }

  /**
   * Acquires all the needed information from the patient resource.
   * @param patient the patient to get info from
   * @return a PatientInfo object containing the age/gender/address of the patient
   * @throws RequestIncompleteException thrown if information is missing.
   */
  public static PatientInfo getPatientInfo(Patient patient) throws RequestIncompleteException {
    Character patientGenderCode = null;
    Address patientAddressState = null;
    Integer patientAge = null;
    String patientId = null;

    if (patient == null) {
      throw new RequestIncompleteException("Patient could not be found.");
    }
    patientGenderCode = patient.getGender().getDisplay().charAt(0);
    patientAddressState = Utilities.getFirstPhysicalHomeAddress(patient.getAddress());
    patientAge = SharedUtilities.calculateAge(patient.getBirthDate());
    patientId = patient.getId();
    if (patientGenderCode == null) {
      throw new RequestIncompleteException("Patient found with no gender. Looking in Patient -> gender");
    }
    if (patientAddressState == null || patientAddressState.getState() == null) {
      throw new RequestIncompleteException("Patient found with no home state. "
          + "Looking in Patient -> address [searching for the first physical home address] -> state.");
    }
    if (patientAge == null) {
      throw new RequestIncompleteException("Patient found with no birthdate. Looking in Patient -> birthDate.");
    }
    if (patientId == null) {
      throw new RequestIncompleteException("Patient found with no ID.");
    }

    return new PatientInfo(patientGenderCode, patientAddressState.getState(), patientAge, patientId);
  }

  /**
   * Extracts information from the practitioner role resource.
   * @param practitionerRole the resource to be searched
   * @return an object containing only the needed information from the resource.
   * @throws RequestIncompleteException thrown if critical information is missing.
   */
  public static PractitionerRoleInfo getPractitionerRoleInfo(
      PractitionerRole practitionerRole, boolean checkLocation) throws RequestIncompleteException {
    Location practitionerRoleLocation = null;
    String locationAddressState = null;

    try {
      practitionerRoleLocation = (Location) practitionerRole.getLocationFirstRep().getResource();
      locationAddressState = practitionerRoleLocation.getAddress().getState();
    } catch (Exception e) {
      //TODO: logger.error("Error parsing needed info from the device request bundle.", e);
    }
    if (practitionerRoleLocation == null & checkLocation) {
      throw new RequestIncompleteException("Practitioner Role Location not found.");
    }
    if (locationAddressState == null & checkLocation) {
      throw new RequestIncompleteException("Patient Role Location found with no Address State.");
    }

    return new PractitionerRoleInfo(locationAddressState);
  }

  public static List<Organization> getPayors(List<Coverage> coverages) {
    List<Organization> payors = new ArrayList<>();
    for (Coverage coverage: coverages){
      if (coverage != null) {
        for (Reference ref: coverage.getPayor()){
          Organization organization = (Organization) ref.getResource();
          if (organization != null) {
            payors.add(organization);
          }
        }
      }
    }
    return payors;
  }

  public static IBaseResource parseFhirData(String resourceString) {
    FhirContext ctx = new FhirComponents().getFhirContext();
    IParser parser = ctx.newJsonParser();
    parser.setParserErrorHandler(new SuppressParserErrorHandler()); // suppress the unknown element warnings
    return parser.parseResource(resourceString);
  }

  public static String getIdFromIBaseResource(IBaseResource baseResource) {
    if (baseResource == null) {
      return "";
    }
    return baseResource.getIdElement().getValue();
  }

  public static FhirResourceInfo getFhirResourceInfo(IBaseResource baseResource) {
    String resourceType = baseResource.fhirType(); // grab the FHIR resource type out of the resource
    resourceType = resourceType.toLowerCase();
    FhirResourceInfo fhirResourceInfo = new FhirResourceInfo();
    fhirResourceInfo.setType(resourceType);

    if (resourceType.equalsIgnoreCase("Questionnaire")) {
      Questionnaire questionnaire = (Questionnaire) baseResource;
      fhirResourceInfo.setId(questionnaire.getId())
          .setName(questionnaire.getName())
          .setUrl(questionnaire.getUrl());
      if (questionnaire.getId() != null) {
        fhirResourceInfo.setId(questionnaire.getIdElement().getIdPart());
      }
    } else if (resourceType.equalsIgnoreCase("Library")) {
      Library library = (Library) baseResource;
      fhirResourceInfo.setId(library.getId())
          .setName(library.getName())
          .setUrl(library.getUrl());
      if (library.getId() != null) {
        fhirResourceInfo.setId(library.getIdElement().getIdPart());
      }
    } else if (resourceType.equalsIgnoreCase("ValueSet")) {
      ValueSet valueSet = (ValueSet) baseResource;
      fhirResourceInfo.setId(valueSet.getId())
          .setName(valueSet.getName())
          .setUrl(valueSet.getUrl());
      if (valueSet.getId() != null) {
        fhirResourceInfo.setId(valueSet.getIdElement().getIdPart());
      }
    } else if (resourceType.equalsIgnoreCase("QuestionnaireResponse")) {
      QuestionnaireResponse questionnaireResponse = (QuestionnaireResponse) baseResource;
      fhirResourceInfo.setId(questionnaireResponse.getId());
      if (questionnaireResponse.getId() != null) {
        fhirResourceInfo.setId(questionnaireResponse.getIdElement().getIdPart());
      }
    }

    return fhirResourceInfo;
  }

  public static FhirResourceInfo getFhirResourceInfo(String resourceString) {
    return getFhirResourceInfo(parseFhirData(resourceString));
  }

  public static Reference convertIdToReference(String id, String fhirType) {
    Reference reference = new Reference();
    if (id.toUpperCase().startsWith(fhirType.toUpperCase() + "/")) {
      reference.setReference(id);
    } else {
      reference.setReference(fhirType + "/" + id);
    }
    return reference;
  }

  public static ClaimResponse createClaimResponse(String priorAuthId, String patientId, String payerId, String providerId, String applicationFhirPath) {
    Date now = new Date();

    ClaimResponse claimResponse = new ClaimResponse();

    claimResponse.setId(priorAuthId);

    Meta meta = new Meta();
    meta.addProfile("http://hl7.org/fhir/us/davinci-pas/StructureDefinition/profile-claimresponse");
    claimResponse.setMeta(meta);

    claimResponse.addIdentifier(new Identifier().setSystem(applicationFhirPath).setValue(claimResponse.getId()));

    claimResponse.setStatus(ClaimResponse.ClaimResponseStatus.ACTIVE);

    CodeableConcept codeableConcept = new CodeableConcept();
    Coding coding = new Coding();
    coding.setSystem("http://terminology.hl7.org/CodeSystem/claim-type");
    coding.setCode("professional");
    coding.setDisplay("Professional");
    codeableConcept.addCoding(coding);
    claimResponse.setType(codeableConcept);

    claimResponse.setUse(ClaimResponse.Use.PREAUTHORIZATION);

    claimResponse.setPatient(convertIdToReference(patientId, "Patient"));

    claimResponse.setCreated(now);

    claimResponse.setInsurer(convertIdToReference(payerId, "Organization"));

    claimResponse.setOutcome(ClaimResponse.RemittanceOutcome.COMPLETE);

    claimResponse.setDisposition("Granted");

    claimResponse.setPreAuthRef(claimResponse.getId());

    ClaimResponse.ItemComponent itemComponent = new ClaimResponse.ItemComponent();

    Extension reviewActionExtension = new Extension();
    reviewActionExtension.setUrl("http://hl7.org/fhir/us/davinci-pas/StructureDefinition/extension-reviewAction");
    reviewActionExtension.addExtension(
        new Extension()
            .setUrl("http://hl7.org/fhir/us/davinci-pas/StructureDefinition/extension-reviewActionCode")
            .setValue(new CodeableConcept().addCoding(
                new Coding()
                    .setSystem("https://valueset.x12.org/x217/005010/response/2000F/HCR/1/01/00/306")
                    .setCode("A1"))));
    reviewActionExtension.addExtension(
        new Extension()
            .setUrl("number")
            .setValue(new StringType(UUID.randomUUID().toString())));
    itemComponent.addExtension(reviewActionExtension);

    itemComponent.addExtension(
        new Extension()
            .setUrl("http://hl7.org/fhir/us/davinci-pas/StructureDefinition/extension-itemPreAuthIssueDate")
            .setValue(new DateType().setValue(now)));

    itemComponent.addExtension(
        new Extension()
            .setUrl("http://hl7.org/fhir/us/davinci-pas/StructureDefinition/extension-authorizationNumber")
            .setValue(new StringType(UUID.randomUUID().toString())));

    Extension providerExtension = new Extension();
    providerExtension.setUrl("http://hl7.org/fhir/us/davinci-pas/StructureDefinition/extension-itemAuthorizedProvider");
    providerExtension.addExtension(
        new Extension()
            .setUrl("provider")
            .setValue(new Reference().setReference(convertIdToReference(providerId, "Practitioner").getReference())));
    itemComponent.addExtension(providerExtension);

    itemComponent.setItemSequence(1);

    itemComponent.addAdjudication(
        new AdjudicationComponent().setCategory(
            new CodeableConcept().addCoding(
                new Coding().setSystem("http://terminology.hl7.org/CodeSystem/adjudication")
                    .setCode("submitted"))));

    claimResponse.addItem(itemComponent);

    return claimResponse;
  }

}
