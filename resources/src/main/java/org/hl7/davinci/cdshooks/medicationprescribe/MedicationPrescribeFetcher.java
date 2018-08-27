package org.hl7.davinci.cdshooks.medicationprescribe;

import org.hl7.davinci.cdshooks.CrdPrefetch;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MedicationPrescribeFetcher pulls the bundle information out of the MedicationPrescribe and fetches
 * any references that are not found in the prefetch.
 */
public class MedicationPrescribeFetcher {

  static final Logger logger = LoggerFactory.getLogger(MedicationPrescribeFetcher.class);

  private MedicationPrescribeContext context;
  private CrdPrefetch prefetch;

  private MedicationRequest medicationRequest = null;

  public MedicationPrescribeFetcher(MedicationPrescribeContext context, CrdPrefetch prefetch) {
    this.context = context;
    this.prefetch = prefetch;


    // loop through the bundles in the context building up the request information
    for (Bundle.BundleEntryComponent bec: context.getMedications().getEntry()) {

      if (bec.hasResource()) {
        switch (bec.getResource().getResourceType()) {
          case MedicationRequest:
            medicationRequest = (MedicationRequest) bec.getResource();
            break;
          default:
            logger.info("fetcher: bundle is of unsupported fhirType: " + bec.getResource().fhirType());
            break;
        }
      }
    }
  }

  public MedicationRequest getMedicationRequest() { return medicationRequest; }

  /**
   * Fetches the remaining resources referenced by the context that are not found in the prefetch.
   */
  public void fetch() {

  }
}
