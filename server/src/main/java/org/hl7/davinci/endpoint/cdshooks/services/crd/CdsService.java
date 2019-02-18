package org.hl7.davinci.endpoint.cdshooks.services.crd;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.validation.Valid;
import org.cdshooks.CdsRequest;
import org.cdshooks.CdsResponse;
import org.cdshooks.Hook;
import org.cdshooks.Prefetch;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.cdsconnect.CdsConnectConnection;
import org.hl7.davinci.endpoint.cdsconnect.CdsConnectRuleQueryFactory;
import org.hl7.davinci.endpoint.components.AbstractCrdRuleQueryFactory;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.CardBuilder.CqlResultsForCard;
import org.hl7.davinci.endpoint.components.PrefetchHydrator;
import org.hl7.davinci.endpoint.database.*;
import org.opencds.cqf.cql.execution.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Component
public abstract class CdsService<requestTypeT extends CdsRequest<?, ?>> {
  static final Logger logger = LoggerFactory.getLogger(CdsService.class);

  /**
   * The {id} portion of the URL to this service which is available at {baseUrl}/cds-services/{id}.
   * REQUIRED
   */
  public String id = null;

  /**
   * The hook this service should be invoked on. REQUIRED
   */
  public Hook hook = null;

  /**
   * The human-friendly name of this service. RECOMMENDED
   */
  public String title = null;

  /**
   * The description of this service. REQUIRED
   */
  public String description = null;

  /**
   * An object containing key/value pairs of FHIR queries that this service is requesting that the
   * EHR prefetch and provide on each service call. The key is a string that describes the type of
   * data being requested and the value is a string representing the FHIR query. OPTIONAL
   */
  public Prefetch prefetch = null;
  Class<?> requestClass = null;

  @Autowired
  private YamlConfig myConfig;

  @Autowired
  RequestService requestService;

  @Autowired
  private CoverageRequirementRuleFinder ruleFinder;
  private List<PrefetchTemplateElement> prefetchElements = null;
  private FhirComponentsT fhirComponents;

  /**
   * The connection to the CDS Connect Service (if enabled).
   */
  private CdsConnectConnection cdsConnectConnection;

  /**
   * A factory to create either CoverageRequirementRuleQuery or CdsConnectRuleQuery objects.
   */
  private AbstractCrdRuleQueryFactory ruleQueryFactory;

  /**
   * Create a new cdsservice.
   *
   * @param id Will be used in the url, should be unique.
   * @param hook Which hook can call this.
   * @param title Human title.
   * @param description Human description.
   * @param prefetchElements List of prefetch elements, will be in prefetch template.
   * @param fhirComponents Fhir components to use
   */
  public CdsService(String id, Hook hook, String title, String description,
      List<PrefetchTemplateElement> prefetchElements, FhirComponentsT fhirComponents) {
    if (id == null) {
      throw new NullPointerException("CDSService id cannot be null");
    }
    if (hook == null) {
      throw new NullPointerException("CDSService hook cannot be null");
    }
    if (description == null) {
      throw new NullPointerException("CDSService description cannot be null");
    }
    this.id = id;
    this.hook = hook;
    this.title = title;
    this.description = description;
    this.prefetchElements = prefetchElements;
    prefetch = new Prefetch();
    for (PrefetchTemplateElement prefetchElement : prefetchElements) {
      this.prefetch.put(prefetchElement.getKey(), prefetchElement.getQuery());
    }
    this.fhirComponents = fhirComponents;
  }

  /**
   * Called after the constructor and all @Autowired instances have been injected.
   */
  @PostConstruct
  public void postConstruct() {
    if (myConfig.getUseCdsConnect()) {
      this.cdsConnectConnection = new CdsConnectConnection(myConfig.getCdsConnectUrl(),
          myConfig.getCdsConnectUsername(), myConfig.getCdsConnectPassword());
      ruleQueryFactory = new CdsConnectRuleQueryFactory(cdsConnectConnection);
    } else {
      ruleQueryFactory = new CoverageRequirementsRuleQueryFactory(ruleFinder);
    }
  }

  public List<PrefetchTemplateElement> getPrefetchElements() {
    return prefetchElements;
  }

  /**
   * Performs generic operations for incoming requests of any type.
   * @param request the generically typed incoming request
   * @return The response from the server
   */
  public CdsResponse handleRequest(@Valid @RequestBody requestTypeT request) {

    PrefetchHydrator prefetchHydrator = new PrefetchHydrator(this, request,
        this.fhirComponents);
    prefetchHydrator.hydrate();

    CdsResponse response = new CdsResponse();
    String launchSmartUrl = smartUrlBuilder(request.getContext().getPatientId(), request.getFhirServer());

    List<Context> cqlExecutionContexts;
    try {
      cqlExecutionContexts = this.createCqlExecutionContexts(request, ruleQueryFactory);
    } catch (RequestIncompleteException e) {
      response.addCard(CardBuilder.summaryCard(e.getMessage()));
      return response;
    }

    boolean foundApplicableRule = false;
    for (Context context: cqlExecutionContexts) {
      CqlResultsForCard results = executeCqlAndGetRelevantResults(context);
      if (results.ruleApplies()){
        foundApplicableRule = true;
        response.addCard(CardBuilder.transform(results, launchSmartUrl));
      }
    }
    if (!foundApplicableRule) {
      response.addCard(CardBuilder.summaryCard("No documentation rules found"));
    }


    CardBuilder.errorCardIfNonePresent(response);
    return response;
  }

  private CqlResultsForCard executeCqlAndGetRelevantResults(Context context) {
    CqlResultsForCard results = new CqlResultsForCard();
    results.setRuleApplies((Boolean) evaluateStatement("RULE_APPLIES",context));
    if (!results.ruleApplies()) {
      return results;
    }
    results.setSummary(evaluateStatement("RESULT_Summary",context).toString())
        .setDetails(evaluateStatement("RESULT_Details",context).toString())
        .setInfoLink(evaluateStatement("RESULT_InfoLink",context).toString());
    return results;
  }

  private Object evaluateStatement(String statement, Context context) {
    return context.resolveExpressionRef(statement).evaluate(context);
  }

  private String smartUrlBuilder(String patientId, String fhirBase) {
    String launchUrl = myConfig.getLaunchUrl();
    if (fhirBase != null && fhirBase.endsWith("/")) {
      fhirBase = fhirBase.substring(0, fhirBase.length() - 1);
    }
    if (patientId != null && patientId.startsWith("Patient/")) {
      patientId = patientId.substring(8,patientId.length());
    }
    return launchUrl + "?iss=" + fhirBase + "&patientId=" + patientId;
  }

  // Implement this in child class
  public abstract List<Context> createCqlExecutionContexts(requestTypeT request, AbstractCrdRuleQueryFactory ruleQueryFactory)
      throws RequestIncompleteException;


}


