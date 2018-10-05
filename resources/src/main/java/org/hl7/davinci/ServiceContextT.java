package org.hl7.davinci;

import org.hl7.fhir.instance.model.api.IBaseBundle;

public interface ServiceContextT<bundleTypeT extends IBaseBundle> {

  String getPatientId();

  void setPatientId(String patientId);

  String getEncounterId();

  void setEncounterId(String encounterId);

  bundleTypeT getServices();

  void setServices(bundleTypeT services);
}
