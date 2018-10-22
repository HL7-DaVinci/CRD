package org.hl7.davinci.r4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hl7.davinci.UtilitiesInterface;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Resource;

import org.hl7.fhir.r4.model.Patient;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class Utilities extends UtilitiesInterface<Resource,Bundle> {
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
  public <T extends Resource> List<T> getResourcesOfTypeFromBundle(
      Class<T> type, Bundle bundle) {
    List<T> retList = new ArrayList<>();
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
   * Gets all resources of some supertype.
   * @param type The class of the resources you want.
   * @param bundle The bundle that might have some the resources you want.
   * @param <T> The class of the resource you want.
   * @return A list of resources of desired type extracted from the bundle.
   */
  public <T extends Resource> List<T> getResourcesOfSuperTypeFromBundle(
      Class<T> type, Bundle bundle) {
    List<T> retList = new ArrayList<>();
    for (BundleEntryComponent bec: bundle.getEntry()) {
      if (!bec.hasResource()) {
        continue;
      }
      Resource resource = bec.getResource();
      if (type.isAssignableFrom(resource.getClass())) {
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
   * Calculate the age of a patient on today's date.
   * @param patient A fhir patient
   * @return The patients age today.
   */
  public static int calculateAge(Patient patient) {
    Date birthDate = patient.getBirthDate();
    if (birthDate == null) {
      return 0;
    }
    LocalDate localBirthDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    return Period.between(localBirthDate, LocalDate.now()).getYears();
  }

}
