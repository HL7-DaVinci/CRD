package org.hl7.davinci.r4.crdhook.orderselect;

import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.fhir.r4.model.Bundle;

/**
 * Class that contains the different prefetch template elements used in crd requests.
 */
public class CrdPrefetchTemplateElements {

  public static final PrefetchTemplateElement MEDICATION_STATEMENT_BUNDLE = new PrefetchTemplateElement(
      "medicationStatementBundle",
      "MedicationStatement?subject={{context.patientId}}"
          + "&_include=MedicationStatement:patient",
      Bundle.class);

  public static final PrefetchTemplateElement MEDICATION_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "medicationRequestBundle",
      "MedicationRequest?_id={{context.draftOrders.MedicationRequest.id}}"
          + "&_include=MedicationRequest:patient"
          + "&_include=MedicationRequest:intended-dispenser"
          + "&_include=MedicationRequest:intended-performer"
          + "&_include=MedicationRequest:performer"
          + "&_include:recurse=PractitionerRole:location"
          + "&_include=MedicationRequest:requester:PractitionerRole"
          + "&_include=MedicationRequest:medication"
          + "&_include=PractitionerRole:organization"
          + "&_include=PractitionerRole:practitioner"
          + "&_include=MedicationRequest:insurance:Coverage",
      Bundle.class);
}