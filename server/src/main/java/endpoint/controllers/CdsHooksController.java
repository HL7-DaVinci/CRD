package endpoint.controllers;

import endpoint.cdshooks.services.crd.MedicationPrescribeService;
import endpoint.cdshooks.services.crd.OrderReviewService;

import javax.validation.Valid;

import org.hl7.davinci.cdshooks.CdsResponse;
import org.hl7.davinci.cdshooks.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.davinci.cdshooks.orderreview.OrderReviewRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CdsHooksController {
  static final String URL_BASE = "/cds-services/";


  @Autowired private OrderReviewService orderReviewService;
  @Autowired private MedicationPrescribeService medicationPrescribeService;

  @CrossOrigin
  @PostMapping(value = URL_BASE + OrderReviewService.ID, consumes = "application/json;charset=UTF-8")
  public CdsResponse handleOrderReview(@Valid @RequestBody OrderReviewRequest request) {
    return orderReviewService.handleRequest(request);
  }

  @CrossOrigin
  @PostMapping(value = URL_BASE + MedicationPrescribeService.ID, consumes = "application/json;charset=UTF-8")
  public CdsResponse handleMedicationPrescribe(@Valid @RequestBody MedicationPrescribeRequest request) {
    return medicationPrescribeService.handleRequest(request);
  }
}
