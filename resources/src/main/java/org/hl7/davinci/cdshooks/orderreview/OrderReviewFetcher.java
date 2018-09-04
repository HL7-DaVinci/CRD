package org.hl7.davinci.cdshooks.orderreview;

import ca.uhn.fhir.rest.client.api.IGenericClient;

import org.hl7.davinci.Utilities;
import org.hl7.davinci.cdshooks.AbstractFetcher;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.util.Pair;

/**
 * The OrderReviewFetcher pulls the bundle information out of the OrderReview and fetches
 * any references that are not found in the prefetch.
 */
public class OrderReviewFetcher extends AbstractFetcher {

  static final Logger logger = LoggerFactory.getLogger(OrderReviewFetcher.class);

  private OrderReviewContext context;

  private DeviceRequest deviceRequest = null;
  private ServiceRequest serviceRequest = null;
  private NutritionOrder nutritionOrder = null;
  private SupplyRequest supplyRequest = null;

  /**
   * Constructor building the fetcher and parsing the bundles out of the context.
   * @param request contains data needed by the fetcher
   */
  public OrderReviewFetcher(OrderReviewRequest request) {
    super(request);
    this.context = request.getContext();

    // loop through the bundles in the context building up the request information
    for (Bundle.BundleEntryComponent bec: context.getOrders().getEntry()) {

      if (bec.hasResource()) {
        switch (bec.getResource().getResourceType()) {
          case DeviceRequest:
            deviceRequest = (DeviceRequest) bec.getResource();
            break;
          case ServiceRequest:
            serviceRequest = (ServiceRequest) bec.getResource();
            break;
          case NutritionOrder:
            nutritionOrder = (NutritionOrder) bec.getResource();
            break;
          case SupplyRequest:
            supplyRequest = (SupplyRequest) bec.getResource();
            break;
          default:
            logger.info("fetcher: bundle is of unsupported fhirType: " + bec.getResource().fhirType());
            break;
        }
      }
    }
  }

  public DeviceRequest getDeviceRequest() { return deviceRequest; }

  public ServiceRequest getServiceRequest() { return serviceRequest; }

  public NutritionOrder getNutritionOrder() { return nutritionOrder; }

  public SupplyRequest getSupplyRequest() { return supplyRequest; }

  /**
   * Fetches the remaining resources referenced by the context that are not found in the prefetch.
   */
  public void fetch() {
    // TODO
    // use the prefetch template to get the data
    // match the data in the prefetch to that in the context
    // fetch the remaining resources from the Provider FHIR server

    // for now just get the patient
//    logger.info("fetcher: patient ID : " + context.getPatientId());
//
//    // look in the context to see what is needed
//    if (deviceRequest != null) {
//      logger.info("fetcher: subject ref: " + deviceRequest.getSubject().getReference());
//
//      boolean fetchPatient = false;
//      // look in the prefetch
//      if (prefetch.getPatient() != null) {
//
//        if (Utilities.compareReferenceToId(deviceRequest.getSubject().getReference(),
//            prefetch.getPatient().getId())) {
//          logger.info("fetch: patient found");
//        } else {
//          // fetch it
//          logger.info("fetching: patient (wrong ID)");
//          logger.info("    patient.ID: '" + prefetch.getPatient().getId() + "'");
//          logger.info("    patientID : '" + deviceRequest.getSubject().getReference() + "'");
//          fetchPatient = true;
//        }
//      } else {
//        // fetch it
//        logger.info("fetching: patient (missing)");
//        fetchPatient = true;
//      }
//
//      if (fetchPatient) {
//        IGenericClient client = composeClient(fhirServer, oauth);
//
//        String pip = deviceRequest.getSubject().getReference();
//        // Change from regex to something more robust in getting references.
//        String[] resourceToGet = pip.split("/");
//        IBaseResource fhirResource = client.read()
//            .resource(resourceToGet[0])
//            .withId(resourceToGet[1])
//            .execute();
//
//        prefetch.setPatient((Patient) fhirResource);
//      }
//
//      // add the patient to the resources map
//      Pair<ResourceType, String> key = new Pair<>(ResourceType.Patient, deviceRequest.getSubject().getReference());
//      resources.put(key, prefetch.getPatient());
//
//      logger.info("fetcher: performer: " + deviceRequest.getPerformer().getReference());
//      logger.info("fetcher: insurance: " + deviceRequest.getInsurance().get(0).getReference());

//    }
  }

  /**
   * Checks if a valid request was provided in the context.
   * @return true if a request was provided, false otherwise.
   */
  public boolean hasRequest() {
    return ((deviceRequest != null) || (serviceRequest != null) || (nutritionOrder != null) || (supplyRequest != null));
  }

}
