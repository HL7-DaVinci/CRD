package org.hl7.davinci.endpoint.cdshooks.services.crd.stu3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.cdshooks.Hook;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.CdsService;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundle;
import org.hl7.davinci.endpoint.cql.stu3.CqlExecutionContextBuilder;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleFinder;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleQuery;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.stu3.FhirComponents;
import org.hl7.davinci.stu3.Utilities;
import org.hl7.davinci.stu3.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.stu3.crdhook.orderselect.OrderSelectRequest;
import org.hl7.davinci.stu3.fhirresources.DaVinciDeviceRequest;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Coverage;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.opencds.cqf.cql.execution.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component("stu3_OrderSelectService")
public class OrderSelectService extends CdsService<OrderSelectRequest>  {

  public static final String ID = "order-select-crd";
  public static final String TITLE = "order-select Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_SELECT;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  static final Logger logger = LoggerFactory.getLogger(OrderSelectService.class);
  public static final FhirComponents FHIRCOMPONENTS = new FhirComponents();
  public static final List<PrefetchTemplateElement> PREFETCH_ELEMENTS = Arrays.asList(
        CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
        CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.PROCEDURE_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.REFERRAL_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.VISION_PRESCRIPTION_BUNDLE
    );

  public OrderSelectService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH_ELEMENTS, FHIRCOMPONENTS);
  }

  @Override
  public List<CoverageRequirementRuleResult> createCqlExecutionContexts(OrderSelectRequest orderSelectRequest, CoverageRequirementRuleFinder ruleFinder) {

    // Note only device requests are currently supported, but you could follow this model to add
    // the others (e.g. supply request), just make sure we have at least one bundle

    // Note: the selections array is currently ignored, all of the draftOrders are processed, not just those selected.
    List<DaVinciDeviceRequest> deviceRequestList = extractDeviceRequests(orderSelectRequest);
    if (deviceRequestList.isEmpty()) {
      throw RequestIncompleteException.NoSupportedBundlesFound();
    }

    List<CoverageRequirementRuleResult> results = new ArrayList<>();
    results.addAll(getDeviceRequestExecutionContexts(deviceRequestList, ruleFinder));

    return results;
  }

  private Context createCqlExecutionContext(CqlBundle cqlPackage, DaVinciDeviceRequest deviceRequest) {
    Patient patient = (Patient) deviceRequest.getSubject().getResource();
    HashMap<String,Resource> cqlParams = new HashMap<>();
    cqlParams.put("Patient", patient);
    cqlParams.put("device_request", deviceRequest);
    return CqlExecutionContextBuilder.getExecutionContext(cqlPackage, cqlParams);
  }

  private List<CoverageRequirementRuleResult> getDeviceRequestExecutionContexts(List<DaVinciDeviceRequest> deviceRequestList, CoverageRequirementRuleFinder ruleFinder) {
    List<CoverageRequirementRuleResult> results = new ArrayList<>();
    for (DaVinciDeviceRequest deviceRequest : deviceRequestList) {
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
            logger.info("stu3/OrderSelectRequest::getDeviceRequestExecutionContexts: failed processing cql bundle: " + e.getMessage());
          }
        }
      }
    }
    return results;
  }

  private List<DaVinciDeviceRequest> extractDeviceRequests(OrderSelectRequest orderSelectRequest) {
    Bundle deviceRequestBundle = orderSelectRequest.getPrefetch().getDeviceRequestBundle();
    List<DaVinciDeviceRequest> deviceRequestList = Utilities
        .getResourcesOfTypeFromBundle(DaVinciDeviceRequest.class, deviceRequestBundle);
    return deviceRequestList;
  }

  private List<CoverageRequirementRuleCriteria> createCriteriaList(DaVinciDeviceRequest deviceRequest) {
    try {
      List<Coding> codings = deviceRequest.getCodeCodeableConcept().getCoding();
      if (codings.size() > 0) {
        logger.info("stu3/OrderSelectRequest::createCriteriaList: code[0]: " + codings.get(0).getCode() + " - " + codings.get(0).getSystem());
      } else {
        logger.info("stu3/OrderSelectRequest::createCriteriaList: empty codes list!");
      }

      List<Coverage> coverages = deviceRequest.getInsurance().stream()
          .map(reference -> (Coverage) reference.getResource()).collect(Collectors.toList());
      List<Organization> payors = Utilities.getPayors(coverages);
      if (payors.size() > 0) {
        logger.info("stu3/OrderSelectRequest::createCriteriaList: payer[0]: " + payors.get(0).getName());
      } else {
        // default to CMS if no payer was provided
        logger.info("stu3/OrderSelectRequest::createCriteriaList: empty payers list, working around by adding CMS!");
        Organization org = new Organization().setName("Centers for Medicare and Medicaid Services");
        org.setId("75f39025-65db-43c8-9127-693cdf75e712");
        payors.add(org);
      }

      List<CoverageRequirementRuleCriteria> criteriaList = CoverageRequirementRuleCriteria
          .createQueriesFromStu3(codings, payors);
      return criteriaList;
    } catch (Exception e) {
      System.out.println(e);
      throw new RequestIncompleteException("Unable to parse list of codes, codesystems, and payors from a device request.");
    }
  }

}
