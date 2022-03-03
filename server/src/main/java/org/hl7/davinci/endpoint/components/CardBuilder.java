package org.hl7.davinci.endpoint.components;

import java.util.*;

import org.cdshooks.*;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.FhirRequestProcessor;
import org.hl7.davinci.endpoint.database.FhirResource;
import org.hl7.davinci.endpoint.database.FhirResourceRepository;
import org.hl7.davinci.r4.Utilities;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience methods for working with CDS Hooks cards.
 */
public class CardBuilder {
  static final Logger logger = LoggerFactory.getLogger(CardBuilder.class);

  public static class CqlResultsForCard {
    private Boolean ruleApplies;

    private CoverageRequirements coverageRequirements;
    private AlternativeTherapy alternativeTherapy;
    private DrugInteraction drugInteraction;

    private IBaseResource request;

    public CqlResultsForCard() {
    }

    public Boolean ruleApplies() {
      return ruleApplies;
    }

    public CqlResultsForCard setRuleApplies(Boolean ruleApplies) {
      if (ruleApplies == null) {
        this.ruleApplies = false;
      } else {
        this.ruleApplies = ruleApplies;
      }
      return this;
    }

    public AlternativeTherapy getAlternativeTherapy() { return alternativeTherapy; }

    public CqlResultsForCard setAlternativeTherapy(AlternativeTherapy alternativeTherapy) {
      this.alternativeTherapy = alternativeTherapy;
      return this;
    }

    public DrugInteraction getDrugInteraction() { return drugInteraction; }

    public CqlResultsForCard setDrugInteraction(DrugInteraction drugInteraction) {
      this.drugInteraction = drugInteraction;
      return this;
    }

    public CoverageRequirements getCoverageRequirements() { return coverageRequirements; }

    public CqlResultsForCard setCoverageRequirements(CoverageRequirements coverageRequirements) {
      this.coverageRequirements = coverageRequirements;
      return this;
    }

    public IBaseResource getRequest() { return request; }

    public CqlResultsForCard setRequest(IBaseResource request) {
      this.request = request;
      return this;
    }
  }

  /**
   * Transforms a result from the database into a card.
   *
   * @param cqlResults
   * @return card with appropriate information
   */
  public static Card transform(CqlResultsForCard cqlResults, Boolean addLink) {
    Card card = baseCard();

    if (addLink) {
      Link link = new Link();
      link.setUrl(cqlResults.getCoverageRequirements().getInfoLink());
      link.setType("absolute");
      link.setLabel("Documentation Requirements");
      card.setLinks(Arrays.asList(link));
    }

    card.setSummary(cqlResults.getCoverageRequirements().getSummary());
    card.setDetail(cqlResults.getCoverageRequirements().getDetails());

    return card;
  }

  /**
   * Transforms a result from the database into a card, defaults to adding the link.
   *
   * @param cqlResults
   * @return card with appropriate information
   */
  public static Card transform(CqlResultsForCard cqlResults) {
    return transform(cqlResults, true);
  }

  /**
   * Transforms a result from the database into a card.
   *
   * @param cqlResults
   * @param smartAppLaunchLink smart app launch Link
   * @return card with appropriate information
   */
  public static Card transform(CqlResultsForCard cqlResults, Link smartAppLaunchLink) {
    Card card = transform(cqlResults);
    List<Link> links = new ArrayList<Link>(card.getLinks());
    links.add(smartAppLaunchLink);
    card.setLinks(links);
    return card;
  }

  /**
   * Tranform the CQL results for card
   * then add a list of smart app launch links to the card
   * @param cqlResults The CQL results
   * @param smartAppLaunchLinks a list of links
   * @return card to be returned
   */
  public static Card transform(CqlResultsForCard cqlResults, List<Link> smartAppLaunchLinks) {
    Card card = transform(cqlResults);
    List<Link> links = new ArrayList<Link>(card.getLinks());
    links.addAll(smartAppLaunchLinks);
    card.setLinks(links);
    return card;
  }

  /**
   * Creates a card with a summary but also has all of the necessary fields populated to be valid.
   *
   * @param summary The desired summary for the card
   * @return valid card
   */
  public static Card summaryCard(String summary) {
    Card card = baseCard();
    card.setSummary(summary);
    return card;
  }

  public static Card alternativeTherapyCard(AlternativeTherapy alternativeTherapy, IBaseResource resource,
                                            FhirComponentsT fhirComponents) {
    logger.info("Build Alternative Therapy Card: " + alternativeTherapy.toString());
    Card card = baseCard();

    card.setSummary("Alternative Therapy Suggested");
    card.setDetail(alternativeTherapy.getDisplay() + " (" + alternativeTherapy.getCode() + ") should be used instead.");

    List<Suggestion> suggestionList = new ArrayList<>();
    Suggestion alternativeTherapySuggestion = new Suggestion();
    alternativeTherapySuggestion.setLabel("Take Suggestion");
    alternativeTherapySuggestion.setIsRecommended(true);
    List<Action> actionList = new ArrayList<>();

    Action deleteAction = new Action(fhirComponents);
    deleteAction.setType(Action.TypeEnum.delete);
    deleteAction.setDescription("Remove original " + resource.fhirType());

    deleteAction.setResource(resource);

    Action createAction = new Action(fhirComponents);
    createAction.setType(Action.TypeEnum.create);
    createAction.setDescription("Add new " + resource.fhirType());
    if (fhirComponents.getFhirVersion() == FhirComponentsT.Version.R4) {
      try {
        createAction.setResource(FhirRequestProcessor.swapTherapyInRequest(resource, alternativeTherapy));
      } catch (RuntimeException e) {
        throw e;
      }
    } else {
      logger.warn("Unsupported fhir version " + fhirComponents.getFhirVersion().toString());
      throw new RuntimeException("Unsupported fhir version " + fhirComponents.getFhirVersion().toString());
    }

    actionList.add(deleteAction);
    actionList.add(createAction);

    alternativeTherapySuggestion.setActions(actionList);
    suggestionList.add(alternativeTherapySuggestion);
    card.setSuggestions(suggestionList);

    card.setSelectionBehavior(Card.SelectionBehaviorEnum.ANY);

    return card;
  }

  public static Card drugInteractionCard(DrugInteraction drugInteraction) {
    logger.info("Build Drug Interaction Card: " + drugInteraction.getSummary());
    Card card = baseCard();
    card.setSummary(drugInteraction.getSummary());
    card.setDetail(drugInteraction.getDetail());
    card.setIndicator(Card.IndicatorEnum.WARNING);
    return card;
  }

  public static Card priorAuthCard(CqlResultsForCard cqlResults,
                                   IBaseResource request,
                                   FhirComponentsT fhirComponents,
                                   String priorAuthId,
                                   String patientId,
                                   String payerId,
                                   String providerId,
                                   String applicationFhirPath,
                                   FhirResourceRepository fhirResourceRepository) {
    logger.info("Build Prior Auth Card");

    Card card = transform(cqlResults, false);

    // create the ClaimResponse
    ClaimResponse claimResponse = Utilities.createClaimResponse(priorAuthId, patientId, payerId, providerId, applicationFhirPath);

    // build the FhirResource and save to the database
    FhirResource fhirResource = new FhirResource();
    fhirResource.setFhirVersion(fhirComponents.getFhirVersion().toString())
        .setResourceType(claimResponse.fhirType())
        .setData(fhirComponents.getJsonParser().encodeResourceToString(claimResponse));
    fhirResource.setId(claimResponse.getId());
    fhirResource.setName(claimResponse.getId());
    FhirResource newFhirResource = fhirResourceRepository.save(fhirResource);
    logger.info("stored: " + newFhirResource.getFhirVersion() + "/" + newFhirResource.getResourceType() + "/" + newFhirResource.getId());

    // create the reference to the ClaimResponse
    Reference claimResponseReference = new Reference();
    claimResponseReference.setReference("ClaimResponse/" + claimResponse.getId());

    // add SupportingInfo to the Request
    IBaseResource outputRequest = FhirRequestProcessor.addSupportingInfoToRequest(request, claimResponseReference);

    // add suggestion with ClaimResponse
    Suggestion suggestionWithClaimResponse = createSuggestionWithResource(outputRequest, claimResponse, fhirComponents,
        "Store the prior authorization in the EHR");
    card.addSuggestionsItem(suggestionWithClaimResponse);

    // add suggestion with annotation
    Suggestion suggestionWithAnnotation = createSuggestionWithNote(card, outputRequest, fhirComponents,
        "Store prior authorization as an annotation to the order", "Add authorization to record",
        false);
    card.addSuggestionsItem(suggestionWithAnnotation);

    card.setSelectionBehavior(Card.SelectionBehaviorEnum.AT_MOST_ONE);

    return card;
  }

  public static Suggestion createSuggestionWithResource(IBaseResource request,
                                                        IBaseResource resource,
                                                        FhirComponentsT fhirComponents,
                                                        String label) {
    Suggestion suggestion = new Suggestion();

    suggestion.setLabel(label);
    suggestion.setIsRecommended(true);

    // build the create Action
    Action createAction = new Action(fhirComponents);
    createAction.setType(Action.TypeEnum.create);
    createAction.setDescription("Store " + resource.fhirType());
    createAction.setResource(resource);
    suggestion.addActionsItem(createAction);

    // build the update Action
    Action updateAction = new Action(fhirComponents);
    updateAction.setType(Action.TypeEnum.update);
    updateAction.setDescription("Update to the resource " + request.fhirType());
    updateAction.setResource(request);
    suggestion.addActionsItem(updateAction);

    return suggestion;
  }
  public static Suggestion createSuggestionWithNote(Card card,
                                                    IBaseResource request,
                                                    FhirComponentsT fhirComponents,
                                                    String label,
                                                    String description,
                                                    boolean isRecommended) {
    Suggestion requestWithNoteSuggestion = new Suggestion();

    requestWithNoteSuggestion.setLabel(label);
    requestWithNoteSuggestion.setIsRecommended(isRecommended);
    List<Action> actionList = new ArrayList<>();

    // build the Annotation
    Annotation annotation = new Annotation();
    StringType author = new StringType();
    author.setValue(card.getSource().getLabel());
    annotation.setAuthor(author);
    String text = card.getSummary() + ": " + card.getDetail();
    annotation.setText(text);
    annotation.setTime(new Date()); // set the date and time to now
    IBaseResource resource = FhirRequestProcessor.addNoteToRequest(request, annotation);

    Action updateAction = new Action(fhirComponents);
    updateAction.setType(Action.TypeEnum.update);
    updateAction.setDescription(description);
    updateAction.setResource(resource);

    actionList.add(updateAction);

    requestWithNoteSuggestion.setActions(actionList);
    return requestWithNoteSuggestion;
  }

  /**
   * Creates an error card and adds it to the response if the response that is passed in does not
   * contain any cards.
   *
   * @param response The response to check and add cards to
   */
  public static void errorCardIfNonePresent(CdsResponse response) {
    if (response.getCards() == null || response.getCards().size() == 0) {
      Card card = new Card();
      card.setIndicator(Card.IndicatorEnum.WARNING);
      Source source = new Source();
      source.setLabel("Da Vinci CRD Reference Implementation");
      card.setSource(source);
      String msg = "Unable to process hook request from provided information.";
      card.setSummary(msg);
      response.addCard(card);
      logger.warn(msg + "; summary card sent to client");
    }
  }

  private static Card baseCard() {
    Card card = new Card();
    card.setIndicator(Card.IndicatorEnum.INFO);
    Source source = new Source();
    source.setLabel("Da Vinci CRD Reference Implementation");
    card.setSource(source);
    return card;
  }
}
