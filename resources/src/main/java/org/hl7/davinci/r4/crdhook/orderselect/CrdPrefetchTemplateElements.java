package org.hl7.davinci.r4.crdhook.orderselect;

import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.fhir.r4.model.Bundle;

/**
 * Class that contains the different prefetch template elements used in crd requests.
 */
public class CrdPrefetchTemplateElements {

  public static final PrefetchTemplateElement COVERAGE_PREFETCH_QUERY = new PrefetchTemplateElement(
      "coverageBundle",
      Bundle.class,
      "Coverage?patient={{context.patient}}");

  public static final PrefetchTemplateElement MEDICATION_STATEMENT_BUNDLE = new PrefetchTemplateElement(
      "medicationStatementBundle",
      Bundle.class,
      "MedicationRequest?_id={{context.medications.MedicationRequest.id}}"
          + "&_include=MedicationRequest:patient"
          + "&_include=MedicationRequest:intended-dispenser"
          + "&_include=MedicationRequest:requester:PractitionerRole"
          + "&_include=MedicationRequest:medication"
          + "&_include:iterate=PractitionerRole:organization"
          + "&_include:iterate=PractitionerRole:practitioner");

  public static final PrefetchTemplateElement MEDICATION_REQUEST_BUNDLE = new PrefetchTemplateElement(
      "medicationRequestBundle",
      Bundle.class,
      "MedicationRequest?_id={{context.draftOrders.MedicationRequest.id}}"
          + "&_include=MedicationRequest:patient"
          + "&_include=MedicationRequest:intended-dispenser"
          + "&_include=MedicationRequest:requester:PractitionerRole"
          + "&_include=MedicationRequest:medication"
          + "&_include:iterate=PractitionerRole:organization"
          + "&_include:iterate=PractitionerRole:practitioner");
}