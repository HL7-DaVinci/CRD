package org.hl7.davinci.endpoint.cdshooks.services.crd;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.cdshooks.*;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.config.YamlConfig;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.CardBuilder.CqlResultsForCard;
import org.hl7.davinci.endpoint.components.PrefetchHydrator;
import org.hl7.davinci.endpoint.database.FhirResourceRepository;
import org.hl7.davinci.endpoint.database.RequestLog;
import org.hl7.davinci.endpoint.database.RequestService;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleCriteria;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.crdhook.orderselect.OrderSelectRequest;
import org.hl7.davinci.r4.crdhook.DiscoveryExtension;
import org.opencds.cqf.cql.engine.execution.Context;
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
  public String id;

  /**
   * The hook this service should be invoked on. REQUIRED
   */
  public Hook hook;

  /**
   * The human-friendly name of this service. RECOMMENDED
   */
  public String title;

  /**
   * The description of this service. REQUIRED
   */
  public String description;

  /**
   * An object containing key/value pairs of FHIR queries that this service is
   * requesting that the EHR prefetch and provide on each service call. The key is
   * a string that describes the type of data being requested and the value is a
   * string representing the FHIR query. OPTIONAL
   */
  public Prefetch prefetch;

  @Autowired
  private YamlConfig myConfig;

  @Autowired
  RequestService requestService;

  @Autowired
  FileStore fileStore;

  @Autowired
  private FhirResourceRepository fhirResourceRepository;

  private final List<PrefetchTemplateElement> prefetchElements;

  protected FhirComponentsT fhirComponents;

  private final DiscoveryExtension extension;

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
   * @param extension        Custom CDS Hooks extensions.
   */
  public CdsService(String id, Hook hook, String title, String description,
      List<PrefetchTemplateElement> prefetchElements, FhirComponentsT fhirComponents,
      DiscoveryExtension extension) {

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
    this.extension = extension;
  }

  public DiscoveryExtension getExtension() { return extension; }

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
      lookupResults = this.createCqlExecutionContexts(request, fileStore, applicationBaseUrl.toString() + "/");
      requestLog.advanceTimeline(requestService);
    } catch (RequestIncompleteException e) {
      logger.warn(e.getMessage() + "; summary card sent to client");
      response.addCard(CardBuilder.summaryCard(e.getMessage()));
      requestLog.setCardListFromCards(response.getCards());
      requestLog.setResults(e.getMessage());
      requestService.edit(requestLog);
      return response;
    }

    // process the extension for the configuration
    Configuration hookConfiguration = new Configuration(); // load hook configuration with default values
    Extension extension = request.getExtension();
    if (extension != null) {
      if (extension.getConfiguration() != null) {
        hookConfiguration = extension.getConfiguration();
      }
    }

    boolean errorCardOnEmpty = !(request instanceof OrderSelectRequest);

    // no error cards on empty when order-select request

    boolean foundApplicableRule = false;
    for (CoverageRequirementRuleResult lookupResult : lookupResults) {
      requestLog.addTopic(requestService, lookupResult.getTopic());
      CqlResultsForCard results = executeCqlAndGetRelevantResults(lookupResult.getContext(), lookupResult.getTopic());
      CoverageRequirements coverageRequirements = results.getCoverageRequirements();

      if (results.ruleApplies()) {
        foundApplicableRule = true;

        if (results.getCoverageRequirements().getApplies()) {

          // if prior auth already approved
          if (coverageRequirements.isPriorAuthApproved()) {
            response.addCard(CardBuilder.priorAuthCard(results, results.getRequest(), fhirComponents, coverageRequirements.getPriorAuthId(),
                request.getContext().getPatientId(), lookupResult.getCriteria().getPayorId(), request.getContext().getUserId(),
                applicationBaseUrl.toString() + "/fhir/" + fhirComponents.getFhirVersion().toString(),
                fhirResourceRepository));

          } else if (coverageRequirements.isDocumentationRequired() || coverageRequirements.isPriorAuthRequired()) {
            if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireOrderUri())
                || StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireFaceToFaceUri())
                || StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireLabUri())
                || StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireProgressNoteUri())
                || StringUtils.isNotEmpty(coverageRequirements.getQuestionnairePARequestUri())
                || StringUtils.isNotEmpty(coverageRequirements.getQuestionnairePlanOfCareUri())
                || StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireDispenseUri())) {
              List<Link> smartAppLinks = createQuestionnaireLinks(request, applicationBaseUrl, lookupResult, results);
              response.addCard(CardBuilder.transform(results, smartAppLinks));

              // add a card for an alternative therapy if there is one
              if (results.getAlternativeTherapy().getApplies() && hookConfiguration.getAlternativeTherapy()) {
                try {
                  response.addCard(CardBuilder.alternativeTherapyCard(results.getAlternativeTherapy(), results.getRequest(),
                      fhirComponents));
                } catch (RuntimeException e) {
                  logger.warn("Failed to process alternative therapy: " + e.getMessage());
                }
              }
            } else {
              logger.warn("Unspecified Questionnaire URI; summary card sent to client");
              response.addCard(CardBuilder.transform(results));
            }
          } else {
            // no prior auth or documentation required
            logger.info("Add the no doc or prior auth required card");
            Card card = CardBuilder.transform(results);
            card.addSuggestionsItem(CardBuilder.createSuggestionWithNote(card, results.getRequest(), fhirComponents,
                "Save Update To EHR", "Update original " + results.getRequest().fhirType() + " to add note"));
            response.addCard(card);
          }
        }

        // apply the DrugInteractions
        if (results.getDrugInteraction().getApplies()) {
          response.addCard(CardBuilder.drugInteractionCard(results.getDrugInteraction()));
        }
      }
    }

    // CQL Executed
    requestLog.advanceTimeline(requestService);

    if (errorCardOnEmpty) {
      if (!foundApplicableRule) {
        String msg = "No documentation rules found";
        logger.warn(msg + "; summary card sent to client");
        response.addCard(CardBuilder.summaryCard(msg));
      }
      CardBuilder.errorCardIfNonePresent(response);
    }

    // Ading card to requestLog
    requestLog.setCardListFromCards(response.getCards());
    requestService.edit(requestLog);


    return response;
  }

  private List<Link> createQuestionnaireLinks(requestTypeT request, URL applicationBaseUrl,
      CoverageRequirementRuleResult lookupResult, CqlResultsForCard results) {
    List<Link> listOfLinks = new ArrayList<>();
    CoverageRequirements coverageRequirements = results.getCoverageRequirements();
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireOrderUri())) {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
          coverageRequirements.getQuestionnaireOrderUri(), coverageRequirements.getRequestId(), lookupResult.getCriteria(),
          coverageRequirements.isPriorAuthRequired(), "Order Form"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireFaceToFaceUri())) {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
          coverageRequirements.getQuestionnaireFaceToFaceUri(), coverageRequirements.getRequestId(), lookupResult.getCriteria(),
          coverageRequirements.isPriorAuthRequired(), "Face to Face Encounter Form"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireLabUri())) {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
          coverageRequirements.getQuestionnaireLabUri(), coverageRequirements.getRequestId(), lookupResult.getCriteria(),
          coverageRequirements.isPriorAuthRequired(), "Lab Form"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireProgressNoteUri())) {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
          coverageRequirements.getQuestionnaireProgressNoteUri(), coverageRequirements.getRequestId(), lookupResult.getCriteria(),
          coverageRequirements.isPriorAuthRequired(), "Progress Note"));
    }

    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnairePARequestUri())) {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
          coverageRequirements.getQuestionnairePARequestUri(), coverageRequirements.getRequestId(), lookupResult.getCriteria(),
          coverageRequirements.isPriorAuthRequired(), "PA Request"));
    }

    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnairePlanOfCareUri())) {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
          coverageRequirements.getQuestionnairePlanOfCareUri(), coverageRequirements.getRequestId(), lookupResult.getCriteria(),
          coverageRequirements.isPriorAuthRequired(), "Plan of Care/Certification"));
    }

    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireDispenseUri())) {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
          coverageRequirements.getQuestionnaireDispenseUri(), coverageRequirements.getRequestId(), lookupResult.getCriteria(),
          coverageRequirements.isPriorAuthRequired(), "Dispense Form"));
    }
    return listOfLinks;
  }

  protected Object evaluateStatement(String statement, Context context) {
    try {
      return context.resolveExpressionRef(statement).evaluate(context);
      // can be thrown if statement is not defined in the cql
    } catch (IllegalArgumentException e) {
      logger.error(e.toString());
      return null;
    } catch (Exception e) {
      logger.error(e.toString());
      return null;
    }
  }

  private Link smartLinkBuilder(String patientId, String fhirBase, URL applicationBaseUrl, String questionnaireUri,
      String reqResourceId, CoverageRequirementRuleCriteria criteria, boolean priorAuthRequired, String label) {
    URI configLaunchUri = myConfig.getLaunchUrl();
    questionnaireUri = applicationBaseUrl + "/fhir/r4/" + questionnaireUri;

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
      patientId = patientId.substring(8);
    }

    // PARAMS:
    // template is the uri of the questionnaire
    // request is the ID of the device request or medrec (not the full URI like the
    // IG says, since it should be taken from fhirBase

    String filepath = "../../getfile/" + criteria.getQueryString();

    String appContext = "template=" + questionnaireUri + "&request=" + reqResourceId;
    appContext = appContext + "&fhirpath=" + applicationBaseUrl + "/fhir/";

    appContext = appContext + "&priorauth=" + (priorAuthRequired ? "true" : "false");
    appContext = appContext + "&filepath=" + applicationBaseUrl + "/";
    if (myConfig.getUrlEncodeAppContext()) {
      logger.info("CdsService::smartLinkBuilder: URL encoding appcontext");
      appContext = URLEncoder.encode(appContext, StandardCharsets.UTF_8).toString();
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

  // Implement these in child class
  public abstract List<CoverageRequirementRuleResult> createCqlExecutionContexts(requestTypeT request,
      FileStore fileStore, String baseUrl) throws RequestIncompleteException;

  protected abstract CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic);
}