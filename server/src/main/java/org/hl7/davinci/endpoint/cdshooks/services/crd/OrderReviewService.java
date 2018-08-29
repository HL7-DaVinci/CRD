package org.hl7.davinci.endpoint.cdshooks.services.crd;

import java.util.HashMap;
import java.util.List;
import org.hl7.davinci.Utilities;
import org.hl7.davinci.cdshooks.PrefetchResponse;
import org.hl7.davinci.Utilities;
import org.hl7.davinci.cdshooks.orderreview.OrderReviewFetcher;
import org.hl7.davinci.endpoint.components.CardBuilder;

import javax.validation.Valid;

import org.hl7.davinci.endpoint.components.prefetchHydrator.PrefetchHydrator;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.cdshooks.CdsResponse;
import org.hl7.davinci.cdshooks.CdsService;
import org.hl7.davinci.cdshooks.Hook;
import org.hl7.davinci.cdshooks.Prefetch;

import org.hl7.davinci.cdshooks.orderreview.OrderReviewRequest;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleFinder;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;


@Component
public class OrderReviewService extends CdsService {

  static final Logger logger = LoggerFactory.getLogger(OrderReviewService.class);

  public static final String ID = "order-review-crd";
  public static final String TITLE = "order-review Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_REVIEW;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final Prefetch PREFETCH;
  static {
    PREFETCH = new Prefetch();
    PREFETCH.put("DeviceRequestBundle","DeviceRequest?id={{context.orders.DeviceRequest.id}}"
        + "&_include=DeviceRequest:patient"
        + "&_include=DeviceRequest:performer"
        + "&_include=DeviceRequest:requester"
        + "&_include=DeviceRequest:device"
        + "&_include=PractitionerRole:organization"
        + "&_include=PractitionerRole:practitioner"
        + "&_include=DeviceRequest:insurance:Coverage");
    PREFETCH.put("MedicationRequestBundle","MedicationRequest?id={{context.orders.MedicationRequest.id}}"
        + "&_include=MedicationRequest:patient"
        + "&_include=MedicationRequest:intended-dispenser"
        + "&_include=MedicationRequest:requester:PractitionerRole"
        + "&_include=MedicationRequest:medication"
        + "&_include=PractitionerRole:organization"
        + "&_include=PractitionerRole:practitioner"
        + "&_include=MedicationRequest:insurance:Coverage");
    PREFETCH.put("NutritionOrderBundle","NutritionOrder?id={{context.orders.NutritionOrder.id}}"
        + "&_include=NutritionOrder:patient"
        + "&_include=NutritionOrder:provider"
        + "&_include=NutritionOrder:requester"
        + "&_include=PractitionerRole:organization"
        + "&_include=PractitionerRole:practitioner"
        + "&_include=NutritionOrder:encounter"
        + "&_include=Encounter:location"
        + "&_include=NutritionOrder:insurance:Coverage");
    PREFETCH.put("ServiceRequestBundle","ServiceRequest?id={{context.orders.ServiceRequest.id}}"
        + "&_include=ServiceRequest:patient"
        + "&_include=ServiceRequest:performer"
        + "&_include=ServiceRequest:requester"
        + "&_include=PractitionerRole:organization"
        + "&_include=PractitionerRole:practitioner"
        + "&_include=ServiceRequest:insurance:Coverage");
    PREFETCH.put("SupplyRequestBundle","SupplyRequest?id={{context.orders.SupplyRequest.id}}&"
        + "_include=SupplyRequest:patient"
        + "&_include=SupplyRequest:supplier:Organization"
        + "&_include=SupplyRequest:requester:Practitioner"
        + "&_include=SupplyRequest:requester:Organization"
        + "&_include=SupplyRequest:Requester:PractitionerRole"
        + "&_include=PractitionerRole:organization"
        + "&_include=PractitionerRole:practitioner"
        + "&_include=SupplyRequest:insurance:Coverage");
  }



  @Autowired
  CoverageRequirementRuleFinder ruleFinder;

  public OrderReviewService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH);
  }

  /**
   * Handle the post request to the service.
   * @param request The json request, parsed.
   * @return
   */
  public CdsResponse handleRequest(@Valid @RequestBody OrderReviewRequest request) {
    logger.info("handleRequest: start");
    logger.info("Order bundle size: " + request.getContext().getOrders().getEntry().size());

    //note currently we only use the device request if its in the prefetch or we get it into
    //the prefetch, so we dont use it if its just in the context since it wont have patient etc.

    PrefetchHydrator prefetchHydrator = new PrefetchHydrator(this, request);
    prefetchHydrator.hydrate(); //prefetch is now as hydrated as possible

    IBaseResource drbResource = request.getPrefetch().get("DeviceRequestBundle");
    if (drbResource.getClass() != Bundle.class) {
      logger.error("Prefetch DeviceRequestBundle not a bundle");
    }
    List<DeviceRequest> deviceRequestList = Utilities.getResourcesOfTypeFromBundle(
        DeviceRequest.class, (Bundle) drbResource);

    CdsResponse response = new CdsResponse();
    for (DeviceRequest deviceRequest: deviceRequestList) {
      // TODO - Replace this with database lookup logic
      response.addCard(CardBuilder.summaryCard("Responses from this service are currently hard coded."));

      CoverageRequirementRule crr = new CoverageRequirementRule();
      crr.setAgeRangeHigh(80);
      crr.setAgeRangeLow(55);
      crr.setEquipmentCode("E0424");
      crr.setGenderCode("F".charAt(0));
      crr.setNoAuthNeeded(false);
      crr.setInfoLink("https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/"
          + "MLNProducts/Downloads/Home-Oxygen-Therapy-Text-Only.pdf");

      response.addCard(CardBuilder.transform(crr));
    }

    logger.info("handleRequest: end");
    return response;



    Patient patient = request.getPrefetch().getPatient();
    CodeableConcept cc = null;
    try {
      cc = request.getContext().firstOrderCode();
    } catch (FHIRException fe) {
      response.addCard(CardBuilder.summaryCard("Unable to parse the device code out of the request"));
    }
    if (patient == null) {
      response.addCard(CardBuilder.summaryCard("No patient could be (pre)fetched in this request"));
    }

    if (patient != null && cc != null) {
      int patientAge = Utilities.calculateAge(patient);
      CoverageRequirementRule crr = ruleFinder.findRule(patientAge, patient.getGender(), cc.getCoding().get(0).getCode());
      if (crr != null) {
        response.addCard(CardBuilder.transform(crr));
      } else {
        response.addCard(CardBuilder.summaryCard("No documentation rules found"));
      }
    }
    logger.info("handleRequest: end");
    return response;
  }

}
