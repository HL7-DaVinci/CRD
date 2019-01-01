package org.hl7.davinci.endpoint.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.cdshooks.Card;
import org.cdshooks.CdsResponse;
import org.cdshooks.Link;
import org.cdshooks.Source;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;

/**
 * Convenience methods for working with CDS Hooks cards.
 */
public class CardBuilder {

  /**
   * Transforms a result from the database into a card.
   *
   * @param crr coverage rule from the database
   * @return card with appropriate information
   */
  public static Card transform(HashMap<String, Object> cqlResult, String launchUrl) {
    Card card = baseCard();

    Link link = new Link();
    link.setUrl(cqlResult.get("RESULT_InfoLink").toString());
    link.setType("absolute");
    link.setLabel("Documentation Requirements");

    Link launchLink = new Link();
    launchLink.setUrl(launchUrl);
    launchLink.setType("smart");
    launchLink.setLabel("SMART App");

    card.setLinks(Arrays.asList(link, launchLink));
    card.setSummary(cqlResult.get("RESULT_Summary").toString());
    card.setDetail(cqlResult.get("RESULT_Detail").toString());
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
