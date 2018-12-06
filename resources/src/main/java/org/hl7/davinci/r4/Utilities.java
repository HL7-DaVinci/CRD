package org.hl7.davinci.r4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hl7.davinci.PatientInfo;
import org.hl7.davinci.PractitionerRoleInfo;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.SharedUtilities;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Address.AddressType;
import org.hl7.fhir.r4.model.Address.AddressUse;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;

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
    String patientAddressState = null;
    Integer patientAge = null;
    String patientId = null;

    if (patient == null) {
      throw new RequestIncompleteException("Patient could not be found.");
    }
    patientGenderCode = patient.getGender().getDisplay().charAt(0);
    patientAddressState = Utilities.getFirstPhysicalHomeAddress(patient.getAddress()).getState();
    patientAge = SharedUtilities.calculateAge(patient.getBirthDate());
    patientId = patient.getId();
    if (patientGenderCode == null) {
      throw new RequestIncompleteException("Patient found with no gender. Looking in Patient -> gender");
    }
    if (patientAddressState == null) {
      throw new RequestIncompleteException("Patient found with no home state. "
          + "Looking in Patient -> address [searching for the first physical home address] -> state.");
    }
    if (patientAge == null) {
      throw new RequestIncompleteException("Patient found with no birthdate. Looking in Patient -> birthDate.");
    }
    if (patientId == null) {
      throw new RequestIncompleteException("Patient found with no ID.");
    }

    return new PatientInfo(patientGenderCode, patientAddressState, patientAge, patientId);
  }

  /**
   * Extracts information from the practitioner role resource.
   * @param practitionerRole the resource to be searched
   * @return an object containing only the needed information from the resource.
   * @throws RequestIncompleteException thrown if critical information is missing.
   */
  public static PractitionerRoleInfo getPractitionerRoleInfo(
      PractitionerRole practitionerRole) throws RequestIncompleteException {
    Location practitionerRoleLocation = null;
    String locationAddressState = null;

    try {
      practitionerRoleLocation = (Location) practitionerRole.getLocationFirstRep().getResource();
      locationAddressState = practitionerRoleLocation.getAddress().getState();
    } catch (Exception e) {
      //TODO: logger.error("Error parsing needed info from the device request bundle.", e);
    }
    if (practitionerRoleLocation == null) {
      throw new RequestIncompleteException("Practitioner Role Location not found.");
    }
    if (locationAddressState == null) {
      throw new RequestIncompleteException("Patient Role Location found with no Address State.");
    }

    return new PractitionerRoleInfo(locationAddressState);
  }

}
