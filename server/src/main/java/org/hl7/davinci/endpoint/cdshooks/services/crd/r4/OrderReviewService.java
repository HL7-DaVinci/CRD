package org.hl7.davinci.endpoint.cdshooks.services.crd.r4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.cdshooks.Hook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.cql.r4.CqlExecutionContextBuilder;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleFinder;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleQuery;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundle;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.r4.crdhook.orderreview.OrderReviewRequest;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.opencds.cqf.cql.execution.Context;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;


@Component("r4_OrderReviewService")
public class OrderReviewService extends CdsService<OrderReviewRequest> {

  public static final String ID = "order-review-crd";
  public static final String TITLE = "order-review Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_REVIEW;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
      CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
      CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.SERVICE_REQUEST_BUNDLE);
  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  static final Logger logger = LoggerFactory.getLogger(OrderReviewService.class);

  public OrderReviewService() { super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS); }

  @Override
  public List<CoverageRequirementRuleResult> createCqlExecutionContexts(OrderReviewRequest orderReviewRequest, CoverageRequirementRuleFinder ruleFinder) {
    // Note only device requests are currently supported, but you could follow this model to add
    // the others (e.g. supply request), just make sure we have at least one bundle
    List<DeviceRequest> deviceRequestList = extractDeviceRequests(orderReviewRequest);
    if (deviceRequestList.isEmpty()) {
      throw RequestIncompleteException.NoSupportedBundlesFound();
    }

    List<CoverageRequirementRuleResult> results = new ArrayList<>();
    results.addAll(getDeviceRequestExecutionContexts(deviceRequestList, ruleFinder));

    return results;
  }

  private Context createCqlExecutionContext(CqlBundle cqlPackage, DeviceRequest deviceRequest) {
    Patient patient = (Patient) deviceRequest.getSubject().getResource();
    HashMap<String,Resource> cqlParams = new HashMap<>();
    cqlParams.put("Patient", patient);
    cqlParams.put("device_request", deviceRequest);
    return CqlExecutionContextBuilder.getExecutionContext(cqlPackage, cqlParams);
  }

  private List<CoverageRequirementRuleResult> getDeviceRequestExecutionContexts(List<DeviceRequest> deviceRequestList, CoverageRequirementRuleFinder ruleFinder) {
    List<CoverageRequirementRuleResult> results = new ArrayList<>();
    for (DeviceRequest deviceRequest : deviceRequestList) {
      List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(deviceRequest);
      for (CoverageRequirementRuleCriteria criteria : criteriaList) {
        CoverageRequirementRuleQuery query = new CoverageRequirementRuleQuery(ruleFinder, criteria);
        query.execute();
        for (CoverageRequirementRule rule: query.getResponse()) {
          CoverageRequirementRuleResult result = new CoverageRequirementRuleResult();
          result.setCriteria(criteria);
          try {
            result.setContext(createCqlExecutionContext(rule.getCqlBundle(), deviceRequest));
            results.add(result);
          } catch (Exception e) {
            logger.info("r4/OrderReviewService::getDeviceRequestExecutionContexts: failed processing cql bundle: " + e.getMessage());
          }
        }
      }
    }
    return results;
  }

  private List<DeviceRequest> extractDeviceRequests(OrderReviewRequest orderReviewRequest) {
    Bundle deviceRequestBundle = orderReviewRequest.getPrefetch().getDeviceRequestBundle();
    List<DeviceRequest> deviceRequestList = Utilities
        .getResourcesOfTypeFromBundle(DeviceRequest.class, deviceRequestBundle);
    return deviceRequestList;
  }

  private List<CoverageRequirementRuleCriteria> createCriteriaList(DeviceRequest deviceRequest) {
    try {
      List<Coding> codings = deviceRequest.getCodeCodeableConcept().getCoding();
      List<Coverage> coverages = deviceRequest.getInsurance().stream()
          .map(reference -> (Coverage) reference.getResource()).collect(Collectors.toList());
      List<Organization> payors = Utilities.getPayors(coverages);
            List<CoverageRequirementRuleCriteria> criteriaList = CoverageRequirementRuleCriteria
          .createQueriesFromR4(codings, payors);
      return criteriaList;
    } catch (Exception e) {
      System.out.println(e);
      throw new RequestIncompleteException("Unable to parse list of codes, codesystems, and payors from a device request.");
    }
  }

}
