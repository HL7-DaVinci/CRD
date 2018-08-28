package org.hl7.davinci.cdshooks.medicationprescribe;

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

}
