package org.hl7.davinci.endpoint.cdshooks.services.crd;

import org.apache.commons.lang.StringUtils;
import org.cdshooks.*;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.AppointmentBookService;
import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.FhirRequestProcessor;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.CardBuilder.CqlResultsForCard;
import org.hl7.davinci.endpoint.components.PrefetchHydrator;
import org.hl7.davinci.endpoint.components.QueryBatchRequest;
import org.hl7.davinci.endpoint.config.YamlConfig;
import org.hl7.davinci.endpoint.database.FhirResourceRepository;
import org.hl7.davinci.endpoint.database.RequestLog;
import org.hl7.davinci.endpoint.database.RequestService;
import org.hl7.davinci.endpoint.files.FileStore;
import org.hl7.davinci.endpoint.rules.CoverageRequirementRuleResult;
import org.hl7.davinci.r4.CardTypes;
import org.hl7.davinci.r4.CoverageGuidance;
import org.hl7.davinci.r4.FhirComponents;
import org.hl7.davinci.r4.crdhook.DiscoveryExtension;
import org.hl7.davinci.r4.crdhook.appointmentbook.AppointmentBookContext;
import org.hl7.davinci.r4.crdhook.orderselect.OrderSelectRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.opencds.cqf.cql.engine.execution.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

  /**
   * Human-friendly description of any preconditions for the use of this CDS Service. OPTIONAL
   */
  public String usageRequirements;

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
   * @param usageRequirements list of preconditions
   * @param fhirComponents   Fhir components to use
   * @param extension        Custom CDS Hooks extensions.
   */
  public CdsService(String id, Hook hook, String title, String description,
      List<PrefetchTemplateElement> prefetchElements, FhirComponentsT fhirComponents,
      DiscoveryExtension extension, String usageRequirements) {

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
    this.usageRequirements = usageRequirements;
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
    // Attempt a Query Batch Request to backfill missing attributes.
    if (myConfig.isQueryBatchRequest()) {
      QueryBatchRequest qbr = new QueryBatchRequest(this.fhirComponents);
      this.attemptQueryBatchRequest(request, qbr);
    }

    logger.info("***** ***** request from requestLog: " + requestLog.toString() );

    CdsResponse response = new CdsResponse();
    CardBuilder cardBuilder = new CardBuilder();

    // CQL Fetched
    List<CoverageRequirementRuleResult> lookupResults;
    try {
      lookupResults = this.createCqlExecutionContexts(request, fileStore, applicationBaseUrl.toString() + "/");
      requestLog.advanceTimeline(requestService);
    } catch (RequestIncompleteException e) {
      logger.warn("RequestIncompleteException " + request);
      logger.warn(e.getMessage() + "; summary card sent to client");
      List<Card> cards = new ArrayList<>();
      cards.add(cardBuilder.summaryCard(CardTypes.COVERAGE, e.getMessage()));
      // Add system actions from card actions
      response.setSystemActions(createSystemActionsFromRequest(request, cards));
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
    int availableCardsLeft = hookConfiguration.getMaxCards();

    for (CoverageRequirementRuleResult lookupResult : lookupResults) {
      requestLog.addTopic(requestService, lookupResult.getTopic());
      CqlResultsForCard results = executeCqlAndGetRelevantResults(lookupResult.getContext(), lookupResult.getTopic());
      CoverageRequirements coverageRequirements = results.getCoverageRequirements();
      cardBuilder.setDeidentifiedResourcesContainsPhi(lookupResult.getDeidentifiedResourceContainsPhi());

      if (!results.ruleApplies())
        continue;

      logger.info(String.valueOf(availableCardsLeft));

      if (availableCardsLeft <= 0)
        break;

      foundApplicableRule = true;

      if (results.getCoverageRequirements().getApplies()) {
        // if prior auth already approved
        if (coverageRequirements.isPriorAuthApproved()) {
          response.addCard(cardBuilder.priorAuthCard(results, results.getRequest(), fhirComponents, coverageRequirements.getPriorAuthId(),
              request.getContext().getPatientId(), lookupResult.getCriteria().getPayorId(), request.getContext().getUserId(),
              applicationBaseUrl.toString() + "/fhir/" + fhirComponents.getFhirVersion().toString(),
              fhirResourceRepository));
          break;
        }

        if (coverageRequirements.isDocumentationRequired() || coverageRequirements.isPriorAuthRequired()) {
          if (!coverageRequirements.hasQuestionnaireUri()) {
            logger.warn("Unspecified Questionnaire URI; summary card sent to client");
            if (hookConfiguration.getCoverage()) {
              response.addCard(cardBuilder.transform(CardTypes.COVERAGE, results));
            }
            break;
          }

          List<Link> smartAppLinks = createQuestionnaireLinks(request, applicationBaseUrl, lookupResult, results);

          if (coverageRequirements.isPriorAuthRequired() && hookConfiguration.getPriorAuth()) {
            Card card = cardBuilder.transform(CardTypes.PRIOR_AUTH, results, smartAppLinks);
            card.addSuggestionsItem(cardBuilder.createSuggestionWithNote(card, results.getRequest(), fhirComponents,
                "Save Update To EHR", "Update original " + results.getRequest().fhirType() + " to add note",
                true, CoverageGuidance.ADMIN));
            response.addCard(card);
            availableCardsLeft--;
          } else if (coverageRequirements.isDocumentationRequired() && hookConfiguration.getDTRClin()) {
            Card card = cardBuilder.transform(CardTypes.DTR_CLIN, results, smartAppLinks);
            card.addSuggestionsItem(cardBuilder.createSuggestionWithNote(card, results.getRequest(), fhirComponents,
                    "Save Update To EHR", "Update original " + results.getRequest().fhirType() + " to add note",
                    true, CoverageGuidance.CLINICAL));
            response.addCard(card);
            availableCardsLeft--;
          }

          // add a card for an alternative therapy if there is one
          if (availableCardsLeft != 0 && results.getAlternativeTherapy().getApplies() && hookConfiguration.getAlternativeTherapy()) {
            try {
              response.addCard(cardBuilder.alternativeTherapyCard(results.getAlternativeTherapy(),
                  results.getRequest(), fhirComponents));
            } catch (RuntimeException e) {
              logger.warn("Failed to process alternative therapy: " + e.getMessage());
            }
          }
          break;
        }

        // no prior auth or documentation required
        logger.info("Add the no doc or prior auth required card");
        if (availableCardsLeft != 0 && hookConfiguration.getCoverage()) {
          Card card = cardBuilder.transform(CardTypes.COVERAGE, results);
          card.addSuggestionsItem(cardBuilder.createSuggestionWithNote(card, results.getRequest(), fhirComponents,
                  "Save Update To EHR", "Update original " + results.getRequest().fhirType() + " to add note",
                  true, CoverageGuidance.COVERED));
          card.setSelectionBehavior(Card.SelectionBehaviorEnum.ANY);
          response.addCard(card);
        }

        logger.info(String.valueOf(availableCardsLeft));
      }

      // apply the DrugInteractions
      if (availableCardsLeft != 0 && results.getDrugInteraction().getApplies()) {
        response.addCard(cardBuilder.drugInteractionCard(results.getDrugInteraction(), results.getRequest()));
        availableCardsLeft--;
      }
    }

    // Add system actions from card actions
    List<Action>  systemActions = createSystemActionsFromCards(response.getCards());
    if (systemActions.isEmpty()) {
      systemActions.add(new Action(this.fhirComponents));
    }
    response.setSystemActions(systemActions);

    // CQL Executed
    requestLog.advanceTimeline(requestService);

    if (errorCardOnEmpty) {
      if (!foundApplicableRule) {
        String msg = "No documentation rules found";
        logger.warn(msg + "; summary card sent to client");
        response.addCard(cardBuilder.summaryCard(CardTypes.COVERAGE, msg));
      }
      cardBuilder.errorCardIfNonePresent(CardTypes.COVERAGE, response);
    }

    // Adding card to requestLog
    requestLog.setCardListFromCards(response.getCards());
    requestService.edit(requestLog);

    return response;
  }

  private List<Action> createSystemActionsFromRequest(requestTypeT request, List<Card> cards) {
    List<Action> systemActions = createSystemActionsFromCards(cards);
    if (systemActions.isEmpty()) {
      if (request.getHook().getValue().equals("appointment-book") && request.getContext() != null) {
        AppointmentBookContext context = (AppointmentBookContext)request.getContext();
        if(context.getAppointments() != null && !context.getAppointments().isEmpty()){
          Bundle appointments = context.getAppointments();
          for (Bundle.BundleEntryComponent e : appointments.getEntry()) {
            Action systemAction = new Action(this.fhirComponents);
            systemAction.setType(Action.TypeEnum.create);
            systemAction.setResource(e.getResource());
            systemActions.add(systemAction);
          }
        }
      }
    }
    return systemActions;
  }

  private List<Link> createQuestionnaireLinks(requestTypeT request, URL applicationBaseUrl,
      CoverageRequirementRuleResult lookupResult, CqlResultsForCard results) {
    List<Link> listOfLinks = new ArrayList<>();
    List<Pair<String, String>> linksToAdd = new ArrayList<>();
    CoverageRequirements coverageRequirements = results.getCoverageRequirements();
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireOrderUri())) {
      linksToAdd.add(Pair.of(coverageRequirements.getQuestionnaireOrderUri(), "Order Form"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireFaceToFaceUri())) {
      linksToAdd.add(Pair.of(coverageRequirements.getQuestionnaireFaceToFaceUri(), "Face to Face Encounter Form"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireLabUri())) {
      linksToAdd.add(Pair.of(coverageRequirements.getQuestionnaireLabUri(),"Lab Form"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireProgressNoteUri())) {
      linksToAdd.add(Pair.of(coverageRequirements.getQuestionnaireProgressNoteUri(),"Progress Note"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnairePARequestUri())) {
      linksToAdd.add(Pair.of(coverageRequirements.getQuestionnairePARequestUri(),"PA Request"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnairePlanOfCareUri())) {
      linksToAdd.add(Pair.of(coverageRequirements.getQuestionnairePlanOfCareUri(),"Plan of Care/Certification"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireDispenseUri())) {
      linksToAdd.add(Pair.of(coverageRequirements.getQuestionnaireDispenseUri(),"Dispense Form"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireAdditionalUri())) {
      linksToAdd.add(Pair.of(coverageRequirements.getQuestionnaireAdditionalUri(),"Additional Form"));
    }
    linksToAdd.forEach((e) -> {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
          e.getFirst(), coverageRequirements.getRequestId(), results.getRequest(), e.getSecond()));
    });
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
                                String reqResourceId, IBaseResource request, String label) {
    URI configLaunchUri = myConfig.getLaunchUrl();
    questionnaireUri = applicationBaseUrl + "/" + questionnaireUri;

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

    // remove the trailing '/' if there is one
    if (fhirBase != null && fhirBase.endsWith("/")) {
      fhirBase = fhirBase.substring(0, fhirBase.length() - 1);
    }
    if (patientId != null && patientId.startsWith("Patient/")) {
      patientId = patientId.substring(8);
    }

    // PARAMS:
    // questionnaire is the canonical uri of the questionnaire resource
    // order is the request (DeviceRequest, ServiceRequest, MedicationRequest, MedicationDispense, etc)
    // coverage is the insurance information
    // can optionally include a "response" parameter for a QuestionnaireResponse resource to relaunch from

    String appContext = "questionnaire=" + questionnaireUri + "&order=" + reqResourceId + "&coverage=" + FhirRequestProcessor.getCoverageFromRequest(request).getReference();

    if (myConfig.getUrlEncodeAppContext()) {
      logger.info("CdsService::smartLinkBuilder: URL encoding appcontext");
      try {
        appContext = URLEncoder.encode(appContext, StandardCharsets.UTF_8.name()).toString();
      } catch (UnsupportedEncodingException e) {
        logger.error("CdsService::smartLinkBuilder: failed to encode URL: " + e.getMessage());
      }
    }

    logger.info("smarLinkBuilder: appContext: " + appContext);

    if (myConfig.isAppendParamsToSmartLaunchUrl()) {
      launchUrl = launchUrl + "?iss=" + fhirBase + "&patientId=" + patientId + "&template=" + questionnaireUri
          + "&request=" + reqResourceId;
    } else {
      // TODO: The iss should be set by the EHR?
      //launchUrl = launchUrl;
    }

    Link link = new Link();
    link.setType("smart");
    link.setLabel(label);
    link.setUrl(launchUrl);

    link.setAppContext(appContext);

    return link;
  }


  protected List<Action> createSystemActionsFromCards(List<Card> cards) {
    List<Action> systemActions = new ArrayList<>();
    for (Card card : cards) {
      if (card.getSuggestions() == null) continue;
      for (Suggestion suggestion : card.getSuggestions()) {
        if (suggestion.getActions() == null) continue;
        for (Action action : suggestion.getActions()) {
          systemActions.add(action);
        }
      }
    }
    return systemActions;
  }

  // Implement these in child class
  public abstract List<CoverageRequirementRuleResult> createCqlExecutionContexts(requestTypeT request,
      FileStore fileStore, String baseUrl) throws RequestIncompleteException;

  protected abstract CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic);

  /**
   * Delegates query batch request to child classes based on their prefetch types.
   */
  protected abstract void attemptQueryBatchRequest(requestTypeT request, QueryBatchRequest qbr);

}