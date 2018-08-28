package org.hl7.davinci.cdshooks.medicationprescribe;

import org.hl7.davinci.cdshooks.AbstractFetcher;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MedicationPrescribeFetcher pulls the bundle information out of the MedicationPrescribe and fetches
 * any references that are not found in the prefetch.
 */
public class MedicationPrescribeFetcher extends AbstractFetcher {

  static final Logger logger = LoggerFactory.getLogger(MedicationPrescribeFetcher.class);

  private MedicationPrescribeContext context;

  private MedicationRequest medicationRequest = null;

  public MedicationPrescribeFetcher(MedicationPrescribeRequest request) {
    super(request);
    this.context = context;


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
    // TODO
    // fetch anything referenced in the context that is missing from the prefetch
    logger.info("fetcher: patient ID : " + context.getPatientId());
  }

  /**
   * Checks if a valid request was provided in the context.
   * @return true if a request was provided, false otherwise.
   */
  public boolean hasRequest() {
    return (medicationRequest != null);
  }

}
