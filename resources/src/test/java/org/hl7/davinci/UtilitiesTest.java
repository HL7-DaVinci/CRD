package org.hl7.davinci;

import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

  @Test
  public void testCalculateAge() {
    LocalDate birthDate = LocalDate.now().minus(40, ChronoUnit.YEARS);
    Patient patient = new Patient();
    patient.setBirthDate(Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    int age = Utilities.calculateAge(patient);
    assertEquals(40, age);
  }
}
