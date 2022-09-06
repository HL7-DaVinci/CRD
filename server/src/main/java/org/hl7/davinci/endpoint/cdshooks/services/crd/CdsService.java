package org.hl7.davinci.endpoint.cdshooks.services.crd;


import org.apache.commons.lang.StringUtils;
import org.cdshooks.*;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.RequestIncompleteException;
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
import org.hl7.davinci.r4.crdhook.DiscoveryExtension;
import org.hl7.davinci.r4.crdhook.orderselect.OrderSelectRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
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
public abstract class CdsService<requestTypeT extends CdsRequest<?, ?>> extends CdsAbstract<requestTypeT> {
  static final Logger logger = LoggerFactory.getLogger(CdsService.class);

  @Autowired
  RequestService requestService;

  @Autowired
  FileStore fileStore;

  @Autowired
  private FhirResourceRepository fhirResourceRepository;

  protected FhirComponentsT fhirComponents;


  public CdsService(String id, Hook hook, String title, String description,
      List<PrefetchTemplateElement> prefetchElements, FhirComponentsT fhirComponents,
      DiscoveryExtension extension) {

    super(id, hook, title, description, prefetchElements, fhirComponents, extension);
    this.fhirComponents = fhirComponents;
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
      this.attempQueryBatchRequest(request, qbr);
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
      response.addCard(cardBuilder.summaryCard(CardTypes.COVERAGE, e.getMessage()));
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
      cardBuilder.setDeidentifiedResourcesContainsPhi(lookupResult.getDeidentifiedResourceContainsPhi());

      if (results.ruleApplies()) {
        foundApplicableRule = true;

        if (results.getCoverageRequirements().getApplies()) {

          // if prior auth already approved
          if (coverageRequirements.isPriorAuthApproved()) {
            response.addCard(cardBuilder.priorAuthCard(results, results.getRequest(), fhirComponents, coverageRequirements.getPriorAuthId(),
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
                || StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireDispenseUri())
                || StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireAdditionalUri())) {
              List<Link> smartAppLinks = createQuestionnaireLinks(request, applicationBaseUrl, lookupResult, results);
              if (coverageRequirements.isPriorAuthRequired()) {
                Card card = cardBuilder.transform(CardTypes.PRIOR_AUTH, results, smartAppLinks);
                card.addSuggestionsItem(cardBuilder.createSuggestionWithNote(card, results.getRequest(), fhirComponents,
                    "Save Update To EHR", "Update original " + results.getRequest().fhirType() + " to add note",
                    true, CoverageGuidance.ADMIN));
                response.addCard(card);
              } else if (coverageRequirements.isDocumentationRequired()) {
                Card card = cardBuilder.transform(CardTypes.DTR_CLIN, results, smartAppLinks);
                card.addSuggestionsItem(cardBuilder.createSuggestionWithNote(card, results.getRequest(), fhirComponents,
                    "Save Update To EHR", "Update original " + results.getRequest().fhirType() + " to add note",
                    true, CoverageGuidance.CLINICAL));
                response.addCard(card);
              }

              // add a card for an alternative therapy if there is one
              if (results.getAlternativeTherapy().getApplies() && hookConfiguration.getAlternativeTherapy()) {
                try {
                  response.addCard(cardBuilder.alternativeTherapyCard(results.getAlternativeTherapy(),
                      results.getRequest(), fhirComponents));
                } catch (RuntimeException e) {
                  logger.warn("Failed to process alternative therapy: " + e.getMessage());
                }
              }
              if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnairePrescriberEnrollmentUri())
                      || StringUtils.isNotEmpty(coverageRequirements.getQuestionnairePrescriberKnowledgeAssessmentUri())) {
                Card prescriberCard = createPrescriberCard(request, applicationBaseUrl, lookupResult, results);
                prescriberCard.setSummary("Prescriber forms");
                response.addCard(prescriberCard);
              }
            } else {
              logger.warn("Unspecified Questionnaire URI; summary card sent to client");
              response.addCard(cardBuilder.transform(CardTypes.COVERAGE, results));
            }
          } else {
            // no prior auth or documentation required
            logger.info("Add the no doc or prior auth required card");
            Card card = cardBuilder.transform(CardTypes.COVERAGE, results);
            card.addSuggestionsItem(cardBuilder.createSuggestionWithNote(card, results.getRequest(), fhirComponents,
                "Save Update To EHR", "Update original " + results.getRequest().fhirType() + " to add note",
                true, CoverageGuidance.COVERED));
            card.setSelectionBehavior(Card.SelectionBehaviorEnum.ANY);
            response.addCard(card);
          }
        }

        // apply the DrugInteractions
        if (results.getDrugInteraction().getApplies()) {
          response.addCard(cardBuilder.drugInteractionCard(results.getDrugInteraction(), results.getRequest()));
        }
      }
    }

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

  private Card createPrescriberCard(requestTypeT request, URL applicationBaseUrl,
                                           CoverageRequirementRuleResult lookupResult, CqlResultsForCard results) {
    List<Link> listOfLinks = new ArrayList<>();
    CardBuilder cardBuilder = new CardBuilder();
    CoverageRequirements coverageRequirements = results.getCoverageRequirements();
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnairePrescriberEnrollmentUri())) {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
              coverageRequirements.getQuestionnairePrescriberEnrollmentUri(), coverageRequirements.getRequestId(),
              results.getRequest(), "Prescriber Enrollment Form"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnairePrescriberKnowledgeAssessmentUri())) {
      listOfLinks.add(smartLinkBuilder(request.getContext().getPatientId(), request.getFhirServer(), applicationBaseUrl,
              coverageRequirements.getQuestionnairePrescriberKnowledgeAssessmentUri(), coverageRequirements.getRequestId(),
              results.getRequest(), "Prescriber Knowledge Assessment Form"));
    }

    Card card;
    if (coverageRequirements.isPriorAuthRequired()) {
      card = cardBuilder.transform(CardTypes.PRIOR_AUTH, results, listOfLinks);
      card.addSuggestionsItem(cardBuilder.createSuggestionWithNote(card, results.getRequest(), fhirComponents,
              "Save Update To EHR", "Update original " + results.getRequest().fhirType() + " to add note",
              true, CoverageGuidance.ADMIN));
    } else {
      card = cardBuilder.transform(CardTypes.DTR_CLIN, results, listOfLinks);
      card.addSuggestionsItem(cardBuilder.createSuggestionWithNote(card, results.getRequest(), fhirComponents,
              "Save Update To EHR", "Update original " + results.getRequest().fhirType() + " to add note",
              true, CoverageGuidance.CLINICAL));
    }
    return card;
  }
  private List<Link> createQuestionnaireLinks(requestTypeT request, URL applicationBaseUrl,
      CoverageRequirementRuleResult lookupResult, CqlResultsForCard results) {
    List<Link> listOfLinks = new ArrayList<>();
    List<Pair<String, String>> linksToAdd = new ArrayList<>();
    CoverageRequirements coverageRequirements = results.getCoverageRequirements();
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireOrderUri())) {
      linksToAdd.add(Pair.of(coverageRequirements.getQuestionnaireOrderUri(), "Patient Enrollment Form"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireFaceToFaceUri())) {
      linksToAdd.add(Pair.of(coverageRequirements.getQuestionnaireFaceToFaceUri(), "Face to Face Encounter Form"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireLabUri())) {
      linksToAdd.add(Pair.of(coverageRequirements.getQuestionnaireLabUri(),"Lab Form"));
    }
    if (StringUtils.isNotEmpty(coverageRequirements.getQuestionnaireProgressNoteUri())) {
      linksToAdd.add(Pair.of(coverageRequirements.getQuestionnaireProgressNoteUri(),"Patient Status Update Form"));
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



  // Implement these in child class
  public abstract List<CoverageRequirementRuleResult> createCqlExecutionContexts(requestTypeT request,
      FileStore fileStore, String baseUrl) throws RequestIncompleteException;

  protected abstract CqlResultsForCard executeCqlAndGetRelevantResults(Context context, String topic);

  /**
   * Delegates query batch request to child classes based on their prefetch types.
   */
  protected abstract void attempQueryBatchRequest(requestTypeT request, QueryBatchRequest qbr);

}
