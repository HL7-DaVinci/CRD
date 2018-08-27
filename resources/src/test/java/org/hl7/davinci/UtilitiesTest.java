package org.hl7.davinci;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilitiesTest {
  @Test
  public void testCompareReferenceToId() {
    assertTrue(Utilities.compareReferenceToId("Patient/1234","1234"));
    assertTrue(Utilities.compareReferenceToId("1234","Patient/1234"));
    assertTrue(Utilities.compareReferenceToId("1234","1234"));
    assertTrue(Utilities.compareReferenceToId("Patient/1234","Patient/1234"));

    assertFalse(Utilities.compareReferenceToId("Patient/1234","4321"));
    assertFalse(Utilities.compareReferenceToId("1234","Patient/4321"));
    assertFalse(Utilities.compareReferenceToId("1234","4321"));
    assertFalse(Utilities.compareReferenceToId("Patient/1234","Patient/4321"));

    assertFalse(Utilities.compareReferenceToId("Patient/1234","Practitioner/1234"));
    assertFalse(Utilities.compareReferenceToId("Patient/1234","Practitioner/4321"));
  }
}
