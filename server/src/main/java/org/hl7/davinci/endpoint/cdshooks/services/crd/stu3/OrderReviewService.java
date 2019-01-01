package org.hl7.davinci.endpoint.cdshooks.services.crd.stu3;

import java.util.ArrayList;
import java.util.HashMap;
import org.cdshooks.Hook;
import org.hl7.davinci.PatientInfo;
import org.hl7.davinci.PractitionerRoleInfo;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CqlRunner;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.CoverageRequirementRuleQuery;
import org.hl7.davinci.stu3.FhirComponents;
import org.hl7.davinci.stu3.Utilities;
import org.hl7.davinci.stu3.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.stu3.crdhook.orderreview.OrderReviewContext;
import org.hl7.davinci.stu3.crdhook.orderreview.OrderReviewRequest;
import org.hl7.fhir.dstu3.model.DaVinciDeviceRequest;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DeviceRequest;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component("stu3_OrderReviewService")
public class OrderReviewService extends CdsService<OrderReviewRequest>  {

  public static final String ID = "order-review-crd";
  public static final String TITLE = "order-review Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_REVIEW;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  static final Logger logger = LoggerFactory.getLogger(OrderReviewService.class);
  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
        CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
        CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.PROCEDURE_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.REFERRAL_REQUEST_BUNDLE
    );

  public OrderReviewService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS);
  }

  private List<DaVinciDeviceRequest> deviceRequestList(OrderReviewRequest orderReviewRequest) {
    Bundle deviceRequestBundle = orderReviewRequest.getPrefetch().getDeviceRequestBundle();
    List<DaVinciDeviceRequest> deviceRequestList = Utilities
        .getResourcesOfTypeFromBundle(DaVinciDeviceRequest.class, deviceRequestBundle);
    return deviceRequestList;
  }

  /**
   * Acquires the specific information needed by the parent request handling
   * function.
   * @param orderReviewRequest the request to extract information from
   * @return a list of the information required.
   * @throws RequestIncompleteException if the request cannot be parsed.
   */
  public List<HashMap<String,Object>> cqlResults(OrderReviewRequest orderReviewRequest)
      throws RequestIncompleteException {
    List<DaVinciDeviceRequest> deviceRequestList = deviceRequestList(orderReviewRequest);
    List<HashMap<String,Object>> cqlResults = new ArrayList<>();
    for (DaVinciDeviceRequest deviceRequest : deviceRequestList) {
      try {
        List<Coding> codings = deviceRequest.getCodeCodeableConcept().getCoding();
        Patient patient = (Patient) deviceRequest.getSubject().getResource();
        PatientInfo patientInfo = Utilities.getPatientInfo(patient);

        List<CoverageRequirementRuleQuery> queries =
            this.resourcesToQueries(codings,
                patient == null,
                true,
                patientInfo,
              new PractitionerRoleInfo());
        for (CoverageRequirementRuleQuery query : queries) {
          query.execute();
          for (CoverageRequirementRule rule: query.getResponse()) {
            cqlResults.add(executeCql(rule.getCql(), deviceRequest));
          }
        }
      } catch (RequestIncompleteException e) {
        throw e;
      } catch (Exception e) {
        logger.error("Error parsing needed info from the device request bundle.", e);
      }
    }
    return cqlResults;
  }

  private HashMap<String,Object> executeCql(String cql, DaVinciDeviceRequest deviceRequest) {
    Resource patientResource = (Resource) deviceRequest.getSubject().getResource();
    CqlRunner cqlRunner = new CqlRunner(cql, patientResource, deviceRequest);
    return cqlRunner.execute();
  }

}
