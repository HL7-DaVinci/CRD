package endpoint.controllers;

import endpoint.cdshooks.services.crd.CrdCdsService;

import javax.validation.Valid;

import org.hl7.davinci.cdshooks.CdsResponse;
import org.hl7.davinci.cdshooks.orderreview.OrderReviewRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CdsHooksController {
  static final String URL_BASE = "/cds-services/";


  @Autowired private CrdCdsService crdCdsService;

  static final Class clz = OrderReviewRequest.class;

  @CrossOrigin
  @PostMapping(value = URL_BASE + CrdCdsService.ID, consumes = "application/json;charset=UTF-8")
  public CdsResponse handleRequest(@Valid @RequestBody OrderReviewRequest request) {
    return crdCdsService.handleRequest(request);
  }
}
