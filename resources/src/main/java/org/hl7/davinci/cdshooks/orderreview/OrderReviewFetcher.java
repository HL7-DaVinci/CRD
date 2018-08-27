package org.hl7.davinci.cdshooks.orderreview;

import org.hl7.davinci.cdshooks.CrdPrefetch;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import javafx.util.Pair;

/**
 * The OrderReviewFetcher pulls the bundle information out of the OrderReview and fetches
 * any references that are not found in the prefetch.
 */
public class OrderReviewFetcher {

  static final Logger logger = LoggerFactory.getLogger(OrderReviewFetcher.class);

  private OrderReviewContext context;
  private CrdPrefetch prefetch;

  private DeviceRequest deviceRequest = null;
  private ServiceRequest serviceRequest = null;
  private NutritionOrder nutritionOrder = null;
  private SupplyRequest supplyRequest = null;

  private Map<Pair<ResourceType, String>, Resource> resources;

  public OrderReviewFetcher(OrderReviewContext context, CrdPrefetch prefetch) {
    this.context = context;
    this.prefetch = prefetch;

    resources = new HashMap<Pair<ResourceType, String>, Resource>();

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

  public Resource getResource(ResourceType type, String reference) {
    Pair<ResourceType, String> key = new Pair<>(type, reference);
    return resources.get(key);
  }

  /**
   * Fetches the remaining resources referenced by the context that are not found in the prefetch.
   */
  public void fetch() {
    //TODO
    // use the prefetch template to get the data
    // match the data in the prefetch to that in the context
    // fetch the remaining resources from the Provider FHIR server

    // for now just get the patient
    logger.info("fetcher: patient ID : " + context.getPatientId());

    // look in the context to see what is needed
    if (deviceRequest != null) {
      logger.info("fetcher: subject ref: " + deviceRequest.getSubject().getReference());

      // look in the prefetch
      if (prefetch.getPatient() != null) {

        if (compareReferenceToId(deviceRequest.getSubject().getReference(),
            prefetch.getPatient().getId())) {
          logger.info("fetch: patient found");
        } else {
          // fetch it
          logger.info("fetching: patient (wrong ID)");
          logger.info("    patient.ID: '" + prefetch.getPatient().getId() + "'");
          logger.info("    patientID : '" + deviceRequest.getSubject().getReference() + "'");

          Patient patient = new Patient();
          patient.setId(deviceRequest.getSubject().getReference());
          prefetch.setPatient(patient);
        }
      } else {
        // fetch it
        logger.info("fetching: patient (missing)");
        Patient patient = new Patient();
        patient.setId(deviceRequest.getSubject().getReference());
        prefetch.setPatient(patient);
      }

      // add the patient to the resources map
      Pair<ResourceType, String> key = new Pair<>(ResourceType.Patient, deviceRequest.getSubject().getReference());
      resources.put(key, prefetch.getPatient());


      logger.info("fetcher: performer: " + deviceRequest.getPerformer().getReference());
      logger.info("fetcher: insurance: " + deviceRequest.getInsurance().get(0).getReference());

    }
  }

  /**
   * Compares the reference to the id of the format: "id", "ResourceType/id"
   * @param reference is a string reference to a resource of type "ResourceType/id"
   * @param id is a string id from a resource of type "id" or "ResourceType/id"
   * @return true if the same
   */
  public static boolean compareReferenceToId(String reference, String id) {
    String refParts[] = reference.split("/");
    String idParts[] = id.split("/");
    if (refParts.length > idParts.length) {
      if (refParts[1].equals(idParts[0])) {
        return true;
      }
    } else if (refParts.length < idParts.length) {
      if (refParts[0].equals(idParts[1])) {
        return true;
      }
    } else { // same length
      if (refParts.length == 1) {
        if (refParts[0].equals(idParts[0])) {
          return true;
        }
      } else {
        if (refParts[0].equals(idParts[0]) && refParts[1].equals(idParts[1])) {
          return true;
        }
      }
    }
    return false;
  }

}
