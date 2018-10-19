package org.hl7.davinci;


import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class SharedUtilities {
  /**
   * Compares the reference to the id of the format: "id", "ResourceType/id".
   * @param reference is a string reference to a resource of type "ResourceType/id"
   * @param id is a string id from a resource of type "id" or "ResourceType/id"
   * @return true if the same
   */
  static boolean compareReferenceToId(String reference, String id) {
    String[] refParts = reference.split("/");
    String[] idParts = id.split("/");
    if (refParts.length > idParts.length) {
      return refParts[1].equals(idParts[0]);
    } else if (refParts.length < idParts.length) {
      return refParts[0].equals(idParts[1]);
    } else { // same length
      if (refParts.length == 1) {
        return refParts[0].equals(idParts[0]);
      } else {
        return refParts[0].equals(idParts[0]) && refParts[1].equals(idParts[1]);
      }
    }
  }

  /**
   * Calculate the age of a patient on today's date.
   * @param birthDate The persons birthday
   * @return The persons age today
   */
  public static int calculateAge(Date birthDate) {
    if (birthDate == null) {
      throw new NullPointerException("birthDate cannot be null");
    }
    LocalDate localBirthDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    return Period.between(localBirthDate, LocalDate.now()).getYears();
  }
}


