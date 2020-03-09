package org.hl7.davinci.endpoint.cdshooks.services.crd;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.cdshooks.*;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.CardBuilder.CqlResultsForCard;
import org.hl7.davinci.endpoint.components.PrefetchHydrator;
import org.hl7.davinci.endpoint.database.RequestLog;
import org.hl7.davinci.endpoint.database.RequestService;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.json.simple.JSONObject;
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
   * The {id} portion of the URL to this service which is available at
   * {baseUrl}/cds-services/{id}. REQUIRED
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
   * An object containing key/value pairs of FHIR queries that this service is
   * requesting that the EHR prefetch and provide on each service call. The key is
   * a string that describes the type of data being requested and the value is a
   * string representing the FHIR query. OPTIONAL
   */
  public Prefetch prefetch = null;
  Class<?> requestClass = null;

  @Autowired
  private YamlConfig myConfig;

  @Autowired
  RequestService requestService;

  @Autowired
  FileStore fileStore;

  private List<PrefetchTemplateElement> prefetchElements = null;
  private FhirComponentsT fhirComponents;

  /**
   * Create a new cdsservice.
   *
   * @param id               Will be used in the url, should be unique.
   * @param hook             Which hook can call this.
   * @param title            Human title.
   * @param description      Human description.
   * @param prefetchElements List of prefetch elements, will be in prefetch
   *                         template.
   * @param fhirComponents   Fhir components to use
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

  public List<PrefetchTemplateElement> getPrefetchElements() {
    return prefetchElements;
  }

  /**
   * Performs generic operations for incoming requests of any type.
   * 
   * @param request the generically typed incoming request
   * @return The response from the server
   */
  public CdsResponse handleRequest(@Valid @RequestBody requestTypeT request, URL applicationBaseUrl) {
    // create the RequestLog
    RequestLog requestLog = new RequestLog(request, new Date().getTime(),
        this.fhirComponents.getFhirVersion().toString(), this.id, requestService, 5);

    // Parsed request
    requestLog.advanceTimeline(requestService);

    PrefetchHydrator prefetchHydrator = new PrefetchHydrator(this, request, this.fhirComponents);
    prefetchHydrator.hydrate();

    // hydrated
    requestLog.advanceTimeline(requestService);

    // logger.info("***** ***** request from requestLog: "+requestLog.toString() );

    CdsResponse response = new CdsResponse();

    // CQL Fetched
    List<CoverageRequirementRuleResult> lookupResults;
    try {
      lookupResults = this.createCqlExecutionContexts(request, fileStore);
      requestLog.advanceTimeline(requestService);
    } catch (RequestIncompleteException e) {
      logger.warn(e.getMessage() + "; summary card sent to client");
      response.addCard(CardBuilder.summaryCard(e.getMessage()));
      requestLog.setResults(e.getMessage());
      requestService.edit(requestLog);
      return response;
    }

    boolean foundApplicableRule = false;
    for (CoverageRequirementRuleResult lookupResult : lookupResults) {
      requestLog.addTopic(requestService, lookupResult.getTopic());
      CqlResultsForCard results = executeCqlAndGetRelevantResults(lookupResult.getContext(), lookupResult.getTopic());
      if (results.ruleApplies()) {
        foundApplicableRule = true;
        if ((results.getDocumentationRequired() || results.getPriorAuthRequired())
            && (StringUtils.isNotEmpty(results.getQuestionnaireOrderUri())
                || StringUtils.isNotEmpty(results.getQuestionnaireFaceToFaceUri())
                || StringUtils.isNotEmpty(results.getQuestionnaireLabUri()))) {
          List<Link> smartAppLinks = createQuestionnaireLinks(request, applicationBaseUrl, lookupResult, results);
          response.addCard(CardBuilder.transform(results, smartAppLinks));
        } else {
          logger.warn("Unspecified Questionnaire URI; summary card sent to client");
          response.addCard(CardBuilder.transform(results));
        }
      }
    }

    // CQL Executed
    requestLog.advanceTimeline(requestService);

    if (!foundApplicableRule) {
      String msg = "No documentation rules found";
      logger.warn(msg + "; summary card sent to client");
      response.addCard(CardBuilder.summaryCard(msg));
    }

    CardBuilder.errorCardIfNonePresent(response);

    return response;
  }

  private List<Link> createQuestionnaireLinks(requestTypeT request, URL applicationBaseUrl,
                                              CoverageRequirementRuleResult lookupResult, CqlResultsForCard results) {
    List<Link> listOfLinks = new ArrayList<>();
    if (StringUtils.isNotEmpty(results.getQuestionnaireOrderUri())) {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
          results.getQuestionnaireOrderUri(), results.getRequestId(), lookupResult.getCriteria(),
          results.getPriorAuthRequired(), "Order Form"));
    }
    if (StringUtils.isNotEmpty(results.getQuestionnaireFaceToFaceUri())) {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
          results.getQuestionnaireFaceToFaceUri(), results.getRequestId(), lookupResult.getCriteria(),
          results.getPriorAuthRequired(), "Face to Face Encounter Form"));
    }
    if (StringUtils.isNotEmpty(results.getQuestionnaireLabUri())) {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
          results.getQuestionnaireLabUri(), results.getRequestId(), lookupResult.getCriteria(),
          results.getPriorAuthRequired(), "Lab Form"));
    }
    return listOfLinks;
  }

  private CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic) {
    CqlResultsForCard results = new CqlResultsForCard();

    results.setRuleApplies((Boolean) evaluateStatement("RULE_APPLIES", context));
    if (!results.ruleApplies()) {
      return results;
    }

    String humanReadableTopic = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(topic), ' ');

    results.setSummary(humanReadableTopic + ": " + evaluateStatement("RESULT_Summary", context).toString())
        .setDetails(evaluateStatement("RESULT_Details", context).toString())
        .setInfoLink(evaluateStatement("RESULT_InfoLink", context).toString())
        .setPriorAuthRequired((Boolean) evaluateStatement("PRIORAUTH_REQUIRED", context))
        .setDocumentationRequired((Boolean) evaluateStatement("DOCUMENTATION_REQUIRED", context));

    if (evaluateStatement("RESULT_QuestionnaireUri", context) != null) {
      results.setQuestionnaireOrderUri(evaluateStatement("RESULT_QuestionnaireUri", context).toString())
          .setRequestId(JSONObject.escape(fhirComponents.getFhirContext().newJsonParser()
              .encodeResourceToString((IBaseResource) evaluateStatement("RESULT_requestId", context))));
    }

    try {
      if (evaluateStatement("RESULT_QuestionnaireFaceToFaceUri", context) != null) {
        results.setQuestionnaireFaceToFaceUri(evaluateStatement("RESULT_QuestionnaireFaceToFaceUri", context).toString())
            .setRequestId(JSONObject.escape(fhirComponents.getFhirContext().newJsonParser()
                .encodeResourceToString((IBaseResource) evaluateStatement("RESULT_requestId", context))));
      }
    } catch (Exception e) {
      logger.info("-- No F2F questionnaire defined");
    }

    try {
      if (evaluateStatement("RESULT_QuestionnaireLabUri", context) != null) {
        results.setQuestionnaireLabUri(evaluateStatement("RESULT_QuestionnaireLabUri", context).toString())
            .setRequestId(JSONObject.escape(fhirComponents.getFhirContext().newJsonParser()
                .encodeResourceToString((IBaseResource) evaluateStatement("RESULT_requestId", context))));
      }
    } catch (Exception e) {
      logger.info("-- No Lab questionnaire defined");
    }

    return results;
  }

  private Object evaluateStatement(String statement, Context context) {
    try {
      return context.resolveExpressionRef(statement).evaluate(context);
      // can be thrown if statement is not defined in the cql
    } catch (IllegalArgumentException e) {
      logger.error(e.toString());
      return null;
    }
  }

  private Link smartLinkBuilder(String patientId, String fhirBase, URL applicationBaseUrl, String questionnaireUri,
      String reqResourceId, CoverageRequirementRuleCriteria criteria, boolean priorAuthRequired, String label) {
    URI configLaunchUri = myConfig.getLaunchUrl();
    String launchUrl;
    if (myConfig.getLaunchUrl().isAbsolute()) {
      launchUrl = myConfig.getLaunchUrl().toString();
    } else {
      try {
        launchUrl = new URL(applicationBaseUrl.getProtocol(), applicationBaseUrl.getHost(),
            applicationBaseUrl.getPort(), applicationBaseUrl.getFile() + configLaunchUri.toString(), null).toString();
      } catch (MalformedURLException e) {
        String msg = "Error creating smart launch URL";
        logger.error(msg);
        throw new RuntimeException(msg);
      }
    }

    if (fhirBase != null && fhirBase.endsWith("/")) {
      fhirBase = fhirBase.substring(0, fhirBase.length() - 1);
    }
    if (patientId != null && patientId.startsWith("Patient/")) {
      patientId = patientId.substring(8, patientId.length());
    }

    // PARAMS:
    // template is the uri of the questionnaire
    // request is the ID of the device request or medrec (not the full URI like the
    // IG says, since it should be taken from fhirBase
    // HashMap<String,String> appContextMap = new HashMap<>();
    // appContextMap.put("template", questionnaireUri);
    // appContextMap.put("request", reqResourceId);
    String filepath = "../../getfile/" + criteria.getQueryString();

    String appContext = "template=" + questionnaireUri + "&request=" + reqResourceId;

    appContext = appContext + "&priorauth=" + (priorAuthRequired ? "true" : "false");
    appContext = appContext + "&filepath=";
    if (myConfig.getUrlEncodeAppContext()) {
      try {
        logger.info("CdsService::smartLinkBuilder: URL encoding appcontext");
        appContext = URLEncoder.encode(appContext, "UTF-8").toString();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }

    if (myConfig.getIncludeFilepathInAppContext()) {
      appContext = appContext + filepath;
    } else {
      appContext = appContext + "_";
    }
    logger.info("smarLinkBuilder: appContext: " + appContext);

    if (myConfig.isAppendParamsToSmartLaunchUrl()) {
      launchUrl = launchUrl + "?iss=" + fhirBase + "&patientId=" + patientId + "&template=" + questionnaireUri
          + "&request=" + reqResourceId;
    } else {
      // TODO: The iss should be set by the EHR?
      launchUrl = launchUrl;
    }

    Link link = new Link();
    link.setType("smart");
    link.setLabel(label);
    link.setUrl(launchUrl);

    link.setAppContext(appContext);

    return link;
  }

  // Implement this in child class
  public abstract List<CoverageRequirementRuleResult> createCqlExecutionContexts(requestTypeT request,
      FileStore fileStore) throws RequestIncompleteException;

}
