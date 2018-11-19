package org.hl7.davinci.stu3.crdhook.medicationprescribe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cdshooks.CdsRequest;
import org.hl7.davinci.stu3.Utilities;
import org.hl7.davinci.stu3.crdhook.CrdPrefetch;
import org.hl7.fhir.dstu3.model.Bundle;

import java.util.HashMap;

public class MedicationPrescribeRequest extends
    CdsRequest<CrdPrefetch, MedicationPrescribeContext> {

  private HashMap<String, Object> mapForPrefetchTemplates = null;

  /**
   * Gets the data from the context to put into the prefetch template.
   * @return a map of prefetch attributes to their values
   */
  @JsonIgnore
  public Object getDataForPrefetchToken() {
    if (mapForPrefetchTemplates != null) {
      return mapForPrefetchTemplates;
    }
    mapForPrefetchTemplates = new HashMap<>();
    mapForPrefetchTemplates.put("user", this.getUser());

    HashMap<String, Object> contextMap = new HashMap<>();
    contextMap.put("patientId", getContext().getPatientId());
    contextMap.put("encounterId", getContext().getEncounterId());
    contextMap.put("medications", Utilities.bundleAsHashmap((Bundle) getContext().getMedications()));
    mapForPrefetchTemplates.put("context", contextMap);

    return mapForPrefetchTemplates;
  }

}
