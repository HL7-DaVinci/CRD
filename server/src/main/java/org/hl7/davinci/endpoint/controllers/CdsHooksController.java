package org.hl7.davinci.endpoint.controllers;

import org.hl7.davinci.endpoint.cdshooks.services.crd.MedicationPrescribeService;
import org.hl7.davinci.endpoint.cdshooks.services.crd.OrderReviewService;

import javax.validation.Valid;

import org.hl7.davinci.cdshooks.CdsResponse;
import org.hl7.davinci.cdshooks.CdsServiceInformation;
import org.hl7.davinci.cdshooks.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.davinci.cdshooks.orderreview.OrderReviewRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CdsHooksController {
  static final String URL_BASE = "/cds-services";


  @Autowired private OrderReviewService orderReviewService;
  @Autowired private MedicationPrescribeService medicationPrescribeService;

  /**
   * The services discovery endpoint.
   * @return A services object containing an array of all services available on this server
   */
  @CrossOrigin
  @GetMapping(value = URL_BASE)
  public CdsServiceInformation serviceDiscovery() {
    CdsServiceInformation serviceInformation = new CdsServiceInformation();
    serviceInformation.addServicesItem(orderReviewService);
    serviceInformation.addServicesItem(medicationPrescribeService);
    return serviceInformation;
  }

  /**
   * The coverage requirement discovery endpoint for the order review hook.
   * @param request An order review triggered cds request
   * @return The card response
   */
  @CrossOrigin
  @PostMapping(value = URL_BASE + "/" + OrderReviewService.ID, consumes = "application/json;charset=UTF-8")
  public CdsResponse handleOrderReview(@Valid @RequestBody OrderReviewRequest request) {
    return orderReviewService.handleRequest(request);
  }

  /**
   * The coverage requirement discovery endpoint for the medication prescribe hook.
   * @param request A medication prescribe triggered cds request
   * @return The card response
   */
  @CrossOrigin
  @PostMapping(value = URL_BASE + "/" + MedicationPrescribeService.ID, consumes = "application/json;charset=UTF-8")
  public CdsResponse handleMedicationPrescribe(@Valid @RequestBody MedicationPrescribeRequest request) {
    return medicationPrescribeService.handleRequest(request);
  }
}
