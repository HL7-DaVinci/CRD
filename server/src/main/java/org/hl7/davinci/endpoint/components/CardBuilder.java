package org.hl7.davinci.endpoint.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cdshooks.Card;
import org.cdshooks.CdsResponse;
import org.cdshooks.Link;
import org.cdshooks.Source;

/**
 * Convenience methods for working with CDS Hooks cards.
 */
public class CardBuilder {

  public static class CqlResultsForCard {
    private Boolean ruleApplies;
    private String summary;
    private String details;
    private String infoLink;
    private String questionnaireUri;
    private String requestId;

    public Boolean ruleApplies() {
      return ruleApplies;
    }

    public CqlResultsForCard setRuleApplies(Boolean ruleApplies) {
      this.ruleApplies = ruleApplies;
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

    public String getQuestionnaireUri() {
      return questionnaireUri;
    }

    public CqlResultsForCard setQuestionnaireUri(String questionnaireUri) {
      this.questionnaireUri = questionnaireUri;
      return this;
    }

    public String getRequestId() {
      return requestId;
    }

    public CqlResultsForCard setRequestId(String requestId) {
      this.requestId = requestId;
      return this;
    }

    public CqlResultsForCard() {
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
      card.setSummary("Unable to process hook request from provided information.");
      response.addCard(card);
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
