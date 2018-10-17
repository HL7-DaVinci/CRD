package org.hl7.davinci.stu3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hl7.davinci.PatientInfo;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.SharedUtilities;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Address.AddressType;
import org.hl7.fhir.dstu3.model.Address.AddressUse;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;

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
        bundleMap.put(resourceType, new ArrayList<Resource>());
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

  public static Address getFirstPhysicalHomeAddress(List<Address> addresses) {
    for (Address address : addresses) {
      if (address.getUse() == AddressUse.HOME && (address.getType() == AddressType.BOTH || address.getType() == AddressType.PHYSICAL)) {
        return address;
      }
    }
    return null;
  }

  public static PatientInfo getPatientInfo(Patient patient) throws RequestIncompleteException{
    Character patientGenderCode = null;
    String patientAddressState = null;
    Integer patientAge = null;

    try {
      patientGenderCode = patient.getGender().getDisplay().charAt(0);;
      patientAddressState = Utilities.getFirstPhysicalHomeAddress(patient.getAddress()).getState();
      patientAge = SharedUtilities.calculateAge(patient.getBirthDate());
    } catch (Exception e){
      //TODO: logger.error("Error parsing needed info from the device request bundle.", e);
    }
    if (patientGenderCode == null) {
      throw new RequestIncompleteException("Patient found with no gender. Looking in Patient -> gender");
    }
    if (patientAddressState == null) {
      throw new RequestIncompleteException("Patient found with no home state. Looking in Patient -> address [searching for the first physical home address] -> state.");
    }
    if (patientAge == null) {
      throw new RequestIncompleteException("Patient found with no birthdate. Looking in Patient -> birthDate.");
    }

    return new PatientInfo(patientGenderCode, patientAddressState, patientAge);
  }

}
