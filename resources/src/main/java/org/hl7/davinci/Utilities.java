package org.hl7.davinci;

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
}
