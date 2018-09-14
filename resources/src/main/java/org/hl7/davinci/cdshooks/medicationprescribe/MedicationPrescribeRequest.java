package org.hl7.davinci.cdshooks.medicationprescribe;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hl7.davinci.Utilities;
import org.hl7.davinci.cdshooks.CdsRequest;
import org.hl7.davinci.cdshooks.CrdPrefetch;

import javax.validation.constraints.NotNull;

public class MedicationPrescribeRequest extends CdsRequest {
  @NotNull
  private MedicationPrescribeContext context = null;

  @Override
  public MedicationPrescribeContext getContext() {
    return context;
  }

  public void setContext(MedicationPrescribeContext context) { this.context = context; }

  private HashMap<String, Object> mapForPrefetchTemplates = null;

  @JsonIgnore
  public Object getDataForPrefetchToken() {
    if (mapForPrefetchTemplates != null) {
      return mapForPrefetchTemplates;
    }
    mapForPrefetchTemplates = new HashMap<>();
    mapForPrefetchTemplates.put("user", this.getUser());

    HashMap<String, Object> contextMap = new HashMap<>();
    contextMap.put("patientId",getContext().getPatientId());
    contextMap.put("encounterId",getContext().getEncounterId());
    contextMap.put("medications",Utilities.bundleAsHashmap(getContext().getMedications()));
    mapForPrefetchTemplates.put("context", contextMap);

    return mapForPrefetchTemplates;
  }

}
