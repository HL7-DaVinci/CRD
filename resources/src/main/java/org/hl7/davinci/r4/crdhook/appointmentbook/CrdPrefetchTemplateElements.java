package org.hl7.davinci.r4.crdhook.appointmentbook;

import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.fhir.r4.model.Bundle;

/**
 * Class that contains the different prefetch template elements used in crd requests.
 * The templates are based on https://build.fhir.org/ig/HL7/davinci-crd/hooks.html#prefetch
 */
public class CrdPrefetchTemplateElements {

	  public static final PrefetchTemplateElement PATIENT_BUNDLE = new PrefetchTemplateElement(
	      "patientBundle",
	      Bundle.class,
	      "Patient/{{context.patientId}}");

  public static final PrefetchTemplateElement COVERAGE_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "coverageBundle",
      Bundle.class,
      "Coverage?patient={{context.patientId}}");

  /*
  public static final PrefetchTemplateElement APPOINTMENT_BUNDLE = new PrefetchTemplateElement(
      "appointmentBundle",
      Bundle.class,
      "Appointment?_id={{context.appointments.Appointment.id}}"
          + "&_include=Appointment:patient"
          + "&_include=Appointment:practitioner:PractitionerRole"
          + "&_include:iterate=PractitionerRole:organization"
          + "&_include:iterate=PractitionerRole:practitioner"
          + "&_include=Appointment:location");
          */

  public static final PrefetchTemplateElement ENCOUNTER_BUNDLE = new PrefetchTemplateElement(
      "encounterBundle",
      Bundle.class,
      "Encounter?_id={{context.encounterId}}"
          + "&_include=Encounter:patient"
          + "&_include=Encounter:service-provider"
          + "&_include=Encounter:practitioner"
          + "&_include=Encounter:location");


}