package org.hl7.davinci.endpoint.controllers.r4;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.cdshooks.CdsResponse;
import org.hl7.davinci.endpoint.Utils;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsServiceInformation;
import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.MedicationPrescribeService;
import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.OrderReviewService;
import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.OrderSelectService;
import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.OrderSignService;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;
import org.hl7.davinci.r4.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.davinci.r4.crdhook.orderreview.OrderReviewRequest;
import org.hl7.davinci.r4.crdhook.orderselect.OrderSelectRequest;
import org.hl7.davinci.r4.crdhook.ordersign.OrderSignRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.stream.Collectors;

@RestController("r4_CdsHooksController")
public class CdsHooksController {

  static final Logger logger = LoggerFactory.getLogger(CdsHooksController.class);

  static final String FHIR_RELEASE = "/r4";
  static final String URL_BASE = "/cds-services";


  @Autowired private OrderReviewService orderReviewService;
  @Autowired private MedicationPrescribeService medicationPrescribeService;
  @Autowired private OrderSelectService orderSelectService;
  @Autowired private OrderSignService orderSignService;

  /**
   * The FHIR r4 services discovery endpoint.
   * @return A services object containing an array of all services available on this server
   */
  @CrossOrigin
  @GetMapping(value = FHIR_RELEASE + URL_BASE)
  public CdsServiceInformation serviceDiscovery() {
    logger.info("r4/serviceDiscovery");
    CdsServiceInformation serviceInformation = new CdsServiceInformation();
    serviceInformation.addServicesItem(orderReviewService);
    serviceInformation.addServicesItem(medicationPrescribeService);
    serviceInformation.addServicesItem(orderSignService);
    serviceInformation.addServicesItem(orderSelectService);
    return serviceInformation;
  }

  /**
   * The coverage requirement discovery endpoint for the order review hook.
   * @param request An order review triggered cds request
   * @return The card response
   */
  @CrossOrigin
  @PostMapping(value = FHIR_RELEASE + URL_BASE + "/" + OrderReviewService.ID,
      consumes = "application/json;charset=UTF-8")
  public CdsResponse handleOrderReview(@Valid @RequestBody OrderReviewRequest request, final HttpServletRequest httpServletRequest) {
    logger.info("r4/handleOrderReview");
    if (request.getPrefetch() == null) {
      request.setPrefetch(new CrdPrefetch());
    }
    return orderReviewService.handleRequest(request, Utils.getApplicationBaseUrl(httpServletRequest));
  }

  /**
   * The coverage requirement discovery endpoint for the medication prescribe hook.
   * @param request A medication prescribe triggered cds request
   * @return The card response
   */
  @CrossOrigin
  @PostMapping(value = FHIR_RELEASE + URL_BASE + "/" + MedicationPrescribeService.ID,
      consumes = "application/json;charset=UTF-8")
  public CdsResponse handleMedicationPrescribe(@Valid @RequestBody MedicationPrescribeRequest request, final HttpServletRequest httpServletRequest) {
    logger.info("r4/handleMedicationPrescribe");
    if (request.getPrefetch() == null) {
      request.setPrefetch(new CrdPrefetch());
    }
    return medicationPrescribeService.handleRequest(request, Utils.getApplicationBaseUrl(httpServletRequest));
  }

  /**
   * The coverage requirement discovery endpoint for the order select hook.
   * @param request An order select triggered cds request
   * @return The card response
   */
  @CrossOrigin
  @PostMapping(value = FHIR_RELEASE + URL_BASE + "/" + OrderSelectService.ID,
      consumes = "application/json;charset=UTF-8")
  public CdsResponse handleOrderSelect(@Valid @RequestBody String request, final HttpServletRequest httpServletRequest) {
    logger.info("r4/handleOrderSelect");

    // test logging
    logger.info(request);

    /*
    if (request.getPrefetch() == null) {
      request.setPrefetch(new CrdPrefetch());
    }
    return orderSelectService.handleRequest(request, Utils.getApplicationBaseUrl(httpServletRequest));
    */
    return null;
  }

  /**
   * The coverage requirement discovery endpoint for the order sign hook.
   * @param request An order sign triggered cds request
   * @return The card response
   */
  @CrossOrigin
  @PostMapping(value = FHIR_RELEASE + URL_BASE + "/" + OrderSignService.ID,
      consumes = "application/json;charset=UTF-8")
  public CdsResponse handleOrderSign(@Valid @RequestBody OrderSignRequest request, final HttpServletRequest httpServletRequest) {
    logger.info("r4/handleOrderSign");
    if (request.getPrefetch() == null) {
      request.setPrefetch(new CrdPrefetch());
    }
    return orderSignService.handleRequest(request, Utils.getApplicationBaseUrl(httpServletRequest));
  }
}
