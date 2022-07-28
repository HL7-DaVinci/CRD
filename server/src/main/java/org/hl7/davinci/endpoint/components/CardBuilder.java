package org.hl7.davinci.endpoint.components;

import java.util.*;

import org.cdshooks.*;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.FhirRequestProcessor;
import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.NoCoverageException;
import org.hl7.davinci.endpoint.database.FhirResource;
import org.hl7.davinci.endpoint.database.FhirResourceRepository;
import org.hl7.davinci.r4.CardTypes;
import org.hl7.davinci.r4.CoverageGuidance;
import org.hl7.davinci.r4.Utilities;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience methods for working with CDS Hooks cards.
 */
public class CardBuilder {
  static final Logger logger = LoggerFactory.getLogger(CardBuilder.class);

  public boolean deidentifiedResourcesContainPhi = false;

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

  public void setDeidentifiedResourcesContainsPhi(boolean deidentifiedResourcesContainPhi) {
    this.deidentifiedResourcesContainPhi = deidentifiedResourcesContainPhi;
  }

  /**
   * Transforms a result from the database into a card.
   *
   * @param cardType
   * @param cqlResults
   * @return card with appropriate information
   */
  public Card transform(CardTypes cardType, CqlResultsForCard cqlResults, Boolean addLink) {
    String requestId = Utilities.getIdFromIBaseResource(cqlResults.getRequest());
    Card card = baseCard(cardType, requestId);

    if (addLink) {
      Link link = new Link();
      link.setUrl(cqlResults.getCoverageRequirements().getInfoLink());
      link.setType("absolute");
      link.setLabel("Documentation Requirements");
      card.setLinks(Arrays.asList(link));
    }

    card.setSummary(cqlResults.getCoverageRequirements().getSummary());
    card.addDetail(cqlResults.getCoverageRequirements().getDetails());

    return card;
  }

  /**
   * Transforms a result from the database into a card, defaults to adding the link.
   *
   * @param cardType
   * @param cqlResults
   * @return card with appropriate information
   */
  public Card transform(CardTypes cardType, CqlResultsForCard cqlResults) {
    return transform(cardType, cqlResults, true);
  }

  /**
   * Transforms a result from the database into a card.
   *
   * @param cardType
   * @param cqlResults
   * @param smartAppLaunchLink smart app launch Link
   * @return card with appropriate information
   */
  public Card transform(CardTypes cardType, CqlResultsForCard cqlResults, Link smartAppLaunchLink) {
    Card card = transform(cardType, cqlResults);
    List<Link> links = new ArrayList<Link>(card.getLinks());
    links.add(smartAppLaunchLink);
    card.setLinks(links);
    return card;
  }

  /**
   * Tranform the CQL results for card
   * then add a list of smart app launch links to the card
   * @param cardType
   * @param cqlResults The CQL results
   * @param smartAppLaunchLinks a list of links
   * @return card to be returned
   */
  public Card transform(CardTypes cardType, CqlResultsForCard cqlResults, List<Link> smartAppLaunchLinks) {
    Card card = transform(cardType, cqlResults);
    List<Link> links = new ArrayList<Link>(card.getLinks());
    links.addAll(smartAppLaunchLinks);
    card.setLinks(links);
    return card;
  }

  /**
   * Creates a card with a summary but also has all of the necessary fields populated to be valid.
   *
   * @param cardType
   * @param summary The desired summary for the card
   * @return valid card
   */
  public Card summaryCard(CardTypes cardType, String summary) {
    Card card = baseCard(cardType, "");
    card.setSummary(summary);
    return card;
  }

  public Card alternativeTherapyCard(AlternativeTherapy alternativeTherapy, IBaseResource resource,
                                            FhirComponentsT fhirComponents) {
    logger.info("Build Alternative Therapy Card: " + alternativeTherapy.toString());
    String requestId = Utilities.getIdFromIBaseResource(resource);
    Card card = baseCard(CardTypes.THERAPY_ALTERNATIVES_OPT, requestId);

    card.setSummary("Alternative Therapy Suggested");
    card.addDetail(alternativeTherapy.getDisplay() + " (" + alternativeTherapy.getCode() + ") should be used instead.");

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

  public Card drugInteractionCard(DrugInteraction drugInteraction, IBaseResource resource) {
    logger.info("Build Drug Interaction Card: " + drugInteraction.getSummary());
    String requestId = Utilities.getIdFromIBaseResource(resource);
    Card card = baseCard(CardTypes.CONTRAINDICATION, requestId);
    card.setSummary(drugInteraction.getSummary());
    card.addDetail(drugInteraction.getDetail());
    card.setIndicator(Card.IndicatorEnum.WARNING);
    return card;
  }

  public Card priorAuthCard(CqlResultsForCard cqlResults,
                                   IBaseResource request,
                                   FhirComponentsT fhirComponents,
                                   String priorAuthId,
                                   String patientId,
                                   String payerId,
                                   String providerId,
                                   String applicationFhirPath,
                                   FhirResourceRepository fhirResourceRepository) {
    logger.info("Build Prior Auth Card");

    Card card = transform(CardTypes.PRIOR_AUTH, cqlResults, false);

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
        "Store the prior authorization in the EHR", true);
    card.addSuggestionsItem(suggestionWithClaimResponse);

    // add suggestion with annotation
    Suggestion suggestionWithAnnotation = createSuggestionWithNote(card, outputRequest, fhirComponents,
        "Store prior authorization as an annotation to the order", "Add authorization to record",
        false, CoverageGuidance.PRIOR_AUTH);
    card.addSuggestionsItem(suggestionWithAnnotation);

    card.setSelectionBehavior(Card.SelectionBehaviorEnum.AT_MOST_ONE);

    return card;
  }

  public Suggestion createSuggestionWithResource(IBaseResource request,
                                                        IBaseResource resource,
                                                        FhirComponentsT fhirComponents,
                                                        String label,
                                                        boolean isRecommended) {
    Suggestion suggestion = new Suggestion();

    suggestion.setLabel(label);
    suggestion.setIsRecommended(isRecommended);

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

  public Suggestion createSuggestionWithNote(Card card,
                                                    IBaseResource request,
                                                    FhirComponentsT fhirComponents,
                                                    String label,
                                                    String description,
                                                    boolean isRecommended,
                                                    CoverageGuidance coverageGuidance) {
    Date now = new Date();
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
    annotation.setTime(now); // set the date and time to now
    IBaseResource resource = FhirRequestProcessor.addNoteToRequest(request, annotation);

    try {
      // build the Extension with the coverage information
      Extension extension = new Extension();
      extension.setUrl("http://hl7.org/fhir/us/davinci-crd/StructureDefinition/ext-coverage-information");

      Extension coverageInfo = new Extension();
      coverageInfo.setUrl("coverageInfo")
          .setValue(coverageGuidance.getCoding());
      extension.addExtension(coverageInfo);

      Extension coverageExtension = new Extension();
      Reference coverageReference = new Reference();
      
      //TODO: get the coverage from the prefetch and pass it into here instead of retrieving it from the request
      coverageReference.setReference(FhirRequestProcessor.getCoverageFromRequest(request).getReference());

      coverageExtension.setUrl("coverage")
          .setValue(coverageReference);
      extension.addExtension(coverageExtension);

      Extension date = new Extension();
      date.setUrl("date")
          .setValue(new DateType().setValue(now));
      extension.addExtension(date);
      Extension identifier = new Extension();
      String id = UUID.randomUUID().toString();
      identifier.setUrl("identifier")
          .setValue(new StringType(id));
      extension.addExtension(identifier);
      resource = FhirRequestProcessor.addExtensionToRequest(resource, extension);
    } catch (NoCoverageException e) {
      logger.warn("No Coverage, discrete coverage extension will not be included: " + e.getMessage());
    }

    Action updateAction = new Action(fhirComponents);
    updateAction.setType(Action.TypeEnum.update);
    updateAction.setDescription(description);
    updateAction.setResource(resource);

    actionList.add(updateAction);

    requestWithNoteSuggestion.setActions(actionList);
    return requestWithNoteSuggestion;
  }

  private Source createSource(CardTypes cardType) {
    Source source = new Source();
    source.setLabel("Da Vinci CRD Reference Implementation");
    source.setTopic(cardType.getCoding());
    return source;
  }

  /**
   * Creates an error card and adds it to the response if the response that is passed in does not
   * contain any cards.
   *
   * @param cardType
   * @param response The response to check and add cards to
   */
  public void errorCardIfNonePresent(CardTypes cardType, CdsResponse response) {
    if (response.getCards() == null || response.getCards().size() == 0) {
      Card card = baseCard(cardType, "");
      card.setIndicator(Card.IndicatorEnum.WARNING);
      String msg = "Unable to process hook request from provided information.";
      card.setSummary(msg);
      response.addCard(card);
      logger.warn(msg + "; summary card sent to client");
    }
  }

  private Card baseCard(CardTypes cardType, String requestId) {
    Card card = new Card();
    card.setIndicator(Card.IndicatorEnum.INFO);
    card.setSource(createSource(cardType));

    if (!requestId.isEmpty()) {
      CardExtension cardExtension = new CardExtension();
      cardExtension.addAssociatedResource(requestId);
      card.setExtension(cardExtension);
    }

    if (deidentifiedResourcesContainPhi) {
      card.setDetail("Note: de-identified resources provided in request contain Protected Health Information (PHI). Please notify administrator.");
    }
    
    return card;
  }
}
