package org.hl7.davinci;

import org.hl7.fhir.r4.model.Bundle;

public interface EncounterBasedServiceContext {
  String getUserId();
  String getPatientId();
  String getEncounterId();
  Bundle getDraftOrders();
}
