package org.hl7.davinci.stu3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.hl7.davinci.stu3.Utilities;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.jupiter.api.Test;

public class UtilitiesTest {

  @Test
  public void testCalculateAge() {
    LocalDate birthDate = LocalDate.now().minus(40, ChronoUnit.YEARS);
    Patient patient = new Patient();
    patient.setBirthDate(Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    int age = Utilities.calculateAge(patient);
    assertEquals(40, age);
  }
}
