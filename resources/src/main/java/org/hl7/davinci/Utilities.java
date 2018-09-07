package org.hl7.davinci;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;

import org.hl7.fhir.r4.model.Patient;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class Utilities {
  /**
   * Compares the reference to the id of the format: "id", "ResourceType/id".
   * @param reference is a string reference to a resource of type "ResourceType/id"
   * @param id is a string id from a resource of type "id" or "ResourceType/id"
   * @return true if the same
   */
  public static boolean compareReferenceToId(String reference, String id) {
    String[] refParts = reference.split("/");
    String[] idParts = id.split("/");
    if (refParts.length > idParts.length) {
      if (refParts[1].equals(idParts[0])) {
        return true;
      }
    } else if (refParts.length < idParts.length) {
      if (refParts[0].equals(idParts[1])) {
        return true;
      }
    } else { // same length
      if (refParts.length == 1) {
        if (refParts[0].equals(idParts[0])) {
          return true;
        }
      } else {
        if (refParts[0].equals(idParts[0]) && refParts[1].equals(idParts[1])) {
          return true;
        }
      }
    }
    return false;
  }

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
