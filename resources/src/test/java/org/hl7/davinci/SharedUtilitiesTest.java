package org.hl7.davinci;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.junit.jupiter.api.Test;

public class SharedUtilitiesTest {

  @Test
  public void testCompareReferenceToId() {
    assertTrue(SharedUtilities.compareReferenceToId("Patient/1234", "1234"));
    assertTrue(SharedUtilities.compareReferenceToId("1234", "Patient/1234"));
    assertTrue(SharedUtilities.compareReferenceToId("1234", "1234"));
    assertTrue(SharedUtilities.compareReferenceToId("Patient/1234", "Patient/1234"));

    assertFalse(SharedUtilities.compareReferenceToId("Patient/1234", "4321"));
    assertFalse(SharedUtilities.compareReferenceToId("1234", "Patient/4321"));
    assertFalse(SharedUtilities.compareReferenceToId("1234", "4321"));
    assertFalse(SharedUtilities.compareReferenceToId("Patient/1234", "Patient/4321"));

    assertFalse(SharedUtilities.compareReferenceToId("Patient/1234", "Practitioner/1234"));
    assertFalse(SharedUtilities.compareReferenceToId("Patient/1234", "Practitioner/4321"));
  }

  @Test
  public void testCalculateAge() {
    LocalDate birthDate = LocalDate.now().minus(40, ChronoUnit.YEARS);
    int age = SharedUtilities
        .calculateAge(Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    assertEquals(40, age);
  }
}
