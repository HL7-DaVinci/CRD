package org.hl7.davinci.endpoint.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cdshooks.Card;
import org.cdshooks.CdsResponse;
import org.cdshooks.Link;
import org.cdshooks.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience methods for working with CDS Hooks cards.
 */
public class CardBuilder {
  static final Logger logger = LoggerFactory.getLogger(CardBuilder.class);

  public static class CqlResultsForCard {
    private Boolean ruleApplies;
    private String summary;
    private String details;
    private String infoLink;
    private String questionnaireOrderUri;
    private String questionnaireFaceToFaceUri;
    private String questionnaireLabUri;
    private String questionnaireProgressNoteUri;
    private String questionnairePARequestUri;
    private String requestId;
    private Boolean priorAuthRequired;
    private Boolean documentationRequired;


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

    public String getSummary() {
      return summary;
    }

    public CqlResultsForCard setSummary(String summary) {
      this.summary = summary;
      return this;
    }

    public String getDetails() {
      return details;
    }

    public CqlResultsForCard setDetails(String details) {
      this.details = details;
      return this;
    }

    public String getInfoLink() {
      return infoLink;
    }

    public CqlResultsForCard setInfoLink(String infoLink) {
      this.infoLink = infoLink;
      return this;
    }

    public String getQuestionnaireOrderUri() {
      return questionnaireOrderUri;
    }

    public CqlResultsForCard setQuestionnaireOrderUri(String questionnaireOrderUri) {
      this.questionnaireOrderUri = questionnaireOrderUri;
      return this;
    }

    public String getRequestId() {
      return requestId;
    }

    public CqlResultsForCard setRequestId(String requestId) {
      this.requestId = requestId;
      return this;
    }

    public boolean getPriorAuthRequired() {
      return priorAuthRequired;
    }

    public CqlResultsForCard setPriorAuthRequired(boolean priorAuthRequired) {
      this.priorAuthRequired = priorAuthRequired;
      return this;
    }

    public boolean getDocumentationRequired() {
      return documentationRequired;
    }

    public CqlResultsForCard setDocumentationRequired(boolean documentationRequired) {
      this.documentationRequired = documentationRequired;
      return this;
    }

    public CqlResultsForCard() {
    }

    public String getQuestionnaireFaceToFaceUri() {
      return this.questionnaireFaceToFaceUri;
    }

    public CqlResultsForCard setQuestionnaireFaceToFaceUri(String questionnaireFaceToFaceUri) {
      this.questionnaireFaceToFaceUri = questionnaireFaceToFaceUri;
      return this;
    }

    public String getQuestionnaireLabUri() {
      return questionnaireLabUri;
    }

    public CqlResultsForCard setQuestionnaireLabUri(String questionnaireLabUri) {
      this.questionnaireLabUri = questionnaireLabUri;
      return this;
    }

    public String getQuestionnaireProgressNoteUri() {
      return questionnaireProgressNoteUri;
    }

    public CqlResultsForCard setQuestionnaireProgressNoteUri(String questionnaireProgressNoteUri) {
      this.questionnaireProgressNoteUri = questionnaireProgressNoteUri;
      return this;
    }

    public String getQuestionnairePARequestUri() {
      return questionnairePARequestUri;
    }

    public CqlResultsForCard setQuestionnairePARequestUri(String questionnairePARequestUri) {
      this.questionnairePARequestUri = questionnairePARequestUri;
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
    link.setUrl(cqlResults.getInfoLink());
    link.setType("absolute");
    link.setLabel("Documentation Requirements");


    card.setLinks(Arrays.asList(link));
    card.setSummary(cqlResults.getSummary());
    card.setDetail(cqlResults.getDetails());
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
