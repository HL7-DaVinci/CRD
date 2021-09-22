package org.hl7.davinci.endpoint.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.cdshooks.*;
import org.hl7.davinci.FhirComponentsT;
import org.hl7.davinci.endpoint.cdshooks.services.crd.r4.FhirRequestProcessor;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.StringType;
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
  public static Card transform(CqlResultsForCard cqlResults) {
    Card card = baseCard();

    Link link = new Link();
    link.setUrl(cqlResults.getCoverageRequirements().getInfoLink());
    link.setType("absolute");
    link.setLabel("Documentation Requirements");

    card.setLinks(Arrays.asList(link));
    card.setSummary(cqlResults.getCoverageRequirements().getSummary());
    card.setDetail(cqlResults.getCoverageRequirements().getDetails());

    return card;
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

  public static Card createSuggestionsWithNote(Card card,
                                               CqlResultsForCard cqlResults,
                                               FhirComponentsT fhirComponents) {
    List<Suggestion> suggestionList = new ArrayList<>();
    Suggestion requestWithNoteSuggestion = new Suggestion();

    requestWithNoteSuggestion.setLabel("Save Update To EHR");
    requestWithNoteSuggestion.setIsRecommended(true);
    List<Action> actionList = new ArrayList<>();

    // build the Annotation
    Annotation annotation = new Annotation();
    StringType author = new StringType();
    author.setValue(card.getSource().getLabel());
    annotation.setAuthor(author);
    String text = card.getSummary() + ": " + card.getDetail();
    annotation.setText(text);
    annotation.setTime(new Date()); // set the date and time to now
    IBaseResource resource = FhirRequestProcessor.addNoteToRequest(cqlResults.getRequest(), annotation);

    Action updateAction = new Action(fhirComponents);
    updateAction.setType(Action.TypeEnum.update);
    updateAction.setDescription("Update original " + cqlResults.getRequest().fhirType() + " to add note");
    updateAction.setResource(resource);

    actionList.add(updateAction);

    requestWithNoteSuggestion.setActions(actionList);
    suggestionList.add(requestWithNoteSuggestion);
    card.setSuggestions(suggestionList);
    return card;
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
