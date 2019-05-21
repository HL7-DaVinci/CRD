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
import org.hl7.davinci.endpoint.cql.CqlExecutionContextBuilder;
import org.hl7.davinci.endpoint.cql.bundle.CqlBundle;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleFinder;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleQuery;
import org.hl7.davinci.stu3.FhirComponents;
import org.hl7.davinci.stu3.Utilities;
import org.hl7.davinci.stu3.crdhook.CrdPrefetchTemplateElements;
import org.hl7.davinci.stu3.crdhook.orderreview.OrderReviewRequest;
import org.hl7.davinci.stu3.fhirresources.DaVinciDeviceRequest;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Coverage;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Resource;
import org.opencds.cqf.cql.execution.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


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

  @Override
  public List<Context> createCqlExecutionContexts(OrderReviewRequest orderReviewRequest, CoverageRequirementRuleFinder ruleFinder) {

    // Note only device requests are currently supported, but you could follow this model to add
    // the others (e.g. supply request), just make sure we have at least one bundle
    List<DaVinciDeviceRequest> deviceRequestList = extractDeviceRequests(orderReviewRequest);
    if (deviceRequestList.isEmpty()) {
      throw RequestIncompleteException.NoSupportedBundlesFound();
    }

    List<Context> contexts = new ArrayList<>();
    contexts.addAll(getDeviceRequestExecutionContexts(deviceRequestList, ruleFinder));

    return contexts;
  }

  private Context createCqlExecutionContext(CqlBundle cqlPackage, DaVinciDeviceRequest deviceRequest) {
    Patient patient = (Patient) deviceRequest.getSubject().getResource();
    PractitionerRole practitionerRole = (PractitionerRole) deviceRequest.getPerformer().getResource();
    Location practitionerLocation = (Location) practitionerRole.getLocation().get(0).getResource();
    HashMap<String,Resource> cqlParams = new HashMap<>();
    cqlParams.put("Patient", patient);
    cqlParams.put("device_request", deviceRequest);
    cqlParams.put("practitioner_location", practitionerLocation);
    return CqlExecutionContextBuilder.getExecutionContextStu3(cqlPackage, cqlParams);
  }

  private List<Context> getDeviceRequestExecutionContexts(List<DaVinciDeviceRequest> deviceRequestList, CoverageRequirementRuleFinder ruleFinder) {
    List<Context> contexts = new ArrayList<>();
    for (DaVinciDeviceRequest deviceRequest : deviceRequestList) {
      List<CoverageRequirementRuleCriteria> criteriaList = createCriteriaList(deviceRequest);
      for (CoverageRequirementRuleCriteria criteria : criteriaList) {
        CoverageRequirementRuleQuery query = new CoverageRequirementRuleQuery(ruleFinder, criteria);
        query.execute();
        for (CoverageRequirementRule rule: query.getResponse()) {
          contexts.add(createCqlExecutionContext(rule.getCqlBundle(), deviceRequest));
        }
      }
    }
    return contexts;
  }

  private List<DaVinciDeviceRequest> extractDeviceRequests(OrderReviewRequest orderReviewRequest) {
    Bundle deviceRequestBundle = orderReviewRequest.getPrefetch().getDeviceRequestBundle();
    List<DaVinciDeviceRequest> deviceRequestList = Utilities
        .getResourcesOfTypeFromBundle(DaVinciDeviceRequest.class, deviceRequestBundle);
    return deviceRequestList;
  }

  private List<CoverageRequirementRuleCriteria> createCriteriaList(DaVinciDeviceRequest deviceRequest) {
    try {
      List<Coding> codings = deviceRequest.getCodeCodeableConcept().getCoding();
      List<Coverage> coverages = deviceRequest.getInsurance().stream()
          .map(reference -> (Coverage) reference.getResource()).collect(Collectors.toList());
      List<Organization> payors = Utilities.getPayors(coverages);
      // workaround for rush
//      Organization org = new Organization().setName("Centers for Medicare and Medicaid Services");
//      org.setId("75f39025-65db-43c8-9127-693cdf75e712");
//      List<Organization> payors = new ArrayList<>();
//      payors.add(org);
      List<CoverageRequirementRuleCriteria> criteriaList = CoverageRequirementRuleCriteria
          .createQueriesFromStu3(codings, payors);
      return criteriaList;
    } catch (Exception e) {
      System.out.println(e);
      throw new RequestIncompleteException("Unable to parse list of codes, codesystems, and payors from a device request.");
    }
  }

}
