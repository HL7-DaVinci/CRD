package org.hl7.davinci.endpoint.controllers.stu3;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.cdshooks.CdsResponse;
import org.hl7.davinci.endpoint.Utils;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsServiceInformation;
import org.hl7.davinci.endpoint.cdshooks.services.crd.stu3.MedicationPrescribeService;
import org.hl7.davinci.endpoint.cdshooks.services.crd.stu3.OrderReviewService;
import org.hl7.davinci.stu3.crdhook.CrdPrefetch;
import org.hl7.davinci.stu3.crdhook.medicationprescribe.MedicationPrescribeRequest;
import org.hl7.davinci.stu3.crdhook.orderreview.OrderReviewRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("stu3_CdsHooksController")
public class CdsHooksController {

  static final Logger logger = LoggerFactory.getLogger(CdsHooksController.class);

  static final String FHIR_RELEASE = "/stu3";
  static final String URL_BASE = "/cds-services";


  @Autowired private OrderReviewService orderReviewService;
  @Autowired private MedicationPrescribeService medicationPrescribeService;

  /**
   * The FHIR STU3 services discovery endpoint.
   * @return A services object containing an array of all services available on this server
   */
  @CrossOrigin
  @GetMapping(value = FHIR_RELEASE + URL_BASE)
  public CdsServiceInformation serviceDiscovery() {
    logger.info("stu3/serviceDiscovery");
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
  @PostMapping(value = FHIR_RELEASE + URL_BASE + "/" + OrderReviewService.ID,
      consumes = "application/json;charset=UTF-8")
  public CdsResponse handleOrderReview(@Valid @RequestBody OrderReviewRequest request, final HttpServletRequest httpServletRequest) {
    logger.info("stu3/handleOrderReview");
    if (request.getPrefetch() == null) {
      request.setPrefetch(new CrdPrefetch());
    }
    return orderReviewService.handleRequest(request, Utils.getApplicationBaseUrl(httpServletRequest));
  }


  /**
   * The coverage requirement discovery endpoint for the order review hook.
   * @param request An order review triggered cds request
   * @return The card response
   */
  @CrossOrigin
  @PostMapping(value = FHIR_RELEASE + URL_BASE + "/" + MedicationPrescribeService.ID,
      consumes = "application/json;charset=UTF-8")
  public CdsResponse handleMedicationPrescribe(@Valid @RequestBody MedicationPrescribeRequest request, final HttpServletRequest httpServletRequest) {
    logger.info("stu3/handleOrderReview");
    if (request.getPrefetch() == null) {
      request.setPrefetch(new CrdPrefetch());
    }
    return medicationPrescribeService.handleRequest(request, Utils.getApplicationBaseUrl(httpServletRequest));
  }
}
