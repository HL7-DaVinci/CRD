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
import org.hl7.davinci.r4.crdhook.orderselect.OrderSelectRequest;
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


@Component("r4_OrderSelectService")
public class OrderSelectService extends CdsService<OrderSelectRequest> {

  public static final String ID = "order-select-crd";
  public static final String TITLE = "order-select Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_SELECT;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
      CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
      CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
      CrdPrefetchTemplateElements.SERVICE_REQUEST_BUNDLE);
  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  static final Logger logger = LoggerFactory.getLogger(OrderSelectService.class);

  public OrderSelectService() { super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS); }

  @Override
  public List<CoverageRequirementRuleResult> createCqlExecutionContexts(OrderSelectRequest orderSelectRequest, CoverageRequirementRuleFinder ruleFinder) {
    // Note only device requests are currently supported, but you could follow this model to add
    // the others (e.g. supply request), just make sure we have at least one bundle

    // Note: the selections array is currently ignored, all of the draftOrders are processed, not just those selected.
    List<DeviceRequest> deviceRequestList = extractDeviceRequests(orderSelectRequest);
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
            logger.info("r4/OrderSelectService::getDeviceRequestExecutionContexts: failed processing cql bundle: " + e.getMessage());
          }
        }
      }
    }
    return results;
  }

  private List<DeviceRequest> extractDeviceRequests(OrderSelectRequest orderSelectRequest) {
    Bundle deviceRequestBundle = orderSelectRequest.getPrefetch().getDeviceRequestBundle();
    List<DeviceRequest> deviceRequestList = Utilities
        .getResourcesOfTypeFromBundle(DeviceRequest.class, deviceRequestBundle);
    return deviceRequestList;
  }

  private List<CoverageRequirementRuleCriteria> createCriteriaList(DeviceRequest deviceRequest) {
    try {List<Coding> codings = deviceRequest.getCodeCodeableConcept().getCoding();
      if (codings.size() > 0) {
        logger.info("r4/OrderSelectService::createCriteriaList: code[0]: " + codings.get(0).getCode() + " - " + codings.get(0).getSystem());
      } else {
        logger.info("r4/OrderSelectService::createCriteriaList: empty codes list!");
      }

      List<Coverage> coverages = deviceRequest.getInsurance().stream()
          .map(reference -> (Coverage) reference.getResource()).collect(Collectors.toList());
      List<Organization> payors = Utilities.getPayors(coverages);
      if (payors.size() > 0) {
        logger.info("r4/OrderSelectService::createCriteriaList: payer[0]: " + payors.get(0).getName());
      } else {
        // default to CMS if no payer was provided
        logger.info("r4/OrderSelectService::createCriteriaList: empty payers list, working around by adding CMS!");
        Organization org = new Organization().setName("Centers for Medicare and Medicaid Services");
        org.setId("75f39025-65db-43c8-9127-693cdf75e712");
        payors.add(org);
      }

      List<CoverageRequirementRuleCriteria> criteriaList = CoverageRequirementRuleCriteria
          .createQueriesFromR4(codings, payors);
      return criteriaList;
    } catch (Exception e) {
      System.out.println(e);
      throw new RequestIncompleteException("Unable to parse list of codes, codesystems, and payors from a device request.");
    }
  }

}
