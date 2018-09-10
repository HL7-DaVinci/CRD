package org.hl7.davinci.endpoint.controllers.stu3;

import org.hl7.davinci.cdshooks.CdsServiceInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.hl7.davinci.endpoint.cdshooks.services.crd.stu3.OrderReviewService;
import org.hl7.davinci.endpoint.cdshooks.services.crd.stu3.MedicationPrescribeService;

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
  public CdsServiceInformation ServiceDiscovery() {
    logger.info("stu3/ServiceDiscovery");
    CdsServiceInformation serviceInformation = new CdsServiceInformation();
    serviceInformation.addServicesItem(orderReviewService);
    serviceInformation.addServicesItem(medicationPrescribeService);
    return serviceInformation;
  }

}
