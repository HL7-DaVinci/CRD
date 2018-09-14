package org.hl7.davinci;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SharedUtilitiesTest {
  @Test
  public void testCompareReferenceToId() {
    assertTrue(SharedUtilities.compareReferenceToId("Patient/1234","1234"));
    assertTrue(SharedUtilities.compareReferenceToId("1234","Patient/1234"));
    assertTrue(SharedUtilities.compareReferenceToId("1234","1234"));
    assertTrue(SharedUtilities.compareReferenceToId("Patient/1234","Patient/1234"));

    assertFalse(SharedUtilities.compareReferenceToId("Patient/1234","4321"));
    assertFalse(SharedUtilities.compareReferenceToId("1234","Patient/4321"));
    assertFalse(SharedUtilities.compareReferenceToId("1234","4321"));
    assertFalse(SharedUtilities.compareReferenceToId("Patient/1234","Patient/4321"));

    assertFalse(SharedUtilities.compareReferenceToId("Patient/1234","Practitioner/1234"));
    assertFalse(SharedUtilities.compareReferenceToId("Patient/1234","Practitioner/4321"));
  }
}
