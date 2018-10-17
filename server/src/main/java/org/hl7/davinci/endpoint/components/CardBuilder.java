package org.hl7.davinci.endpoint.components;

import java.util.ArrayList;
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
  public static Card transform(CoverageRequirementRule crr) {
    Card card = baseCard();
    if (crr.getNoAuthNeeded()) {
      String summary = String
          .format("No documentation is required for a device or service with code: %s",
              crr.getEquipmentCode());
      card.setSummary(summary);
    } else {
      card.setSummary("Documentation is required for the desired device or service");
      Link link = new Link();
      link.setUrl(crr.getInfoLink());
      link.setType("absolute");
      link.setLabel("Documentation Requirements");
      List<Link> links = new ArrayList<>();
      links.add(link);
      card.setLinks(links);
      String detail = "There are documentation requirements for the following criteria:"
          + "\n Patient is of gender: '%s' and between the ages of: %d and %d and lives in state: '%s'"
          + "\n Device or service has code of '%s'"
          + "\n Service is requested in state: '%s'.";
      String genderRule = "Any";
      if (crr.getGenderCode() != null) {
        genderRule = crr.getGenderCode().toString();
      }
      String patientStateRule = Optional.ofNullable(crr.getPatientAddressState()).orElse("Any");
      String providerStateRule = Optional.ofNullable(crr.getProviderAddressState()).orElse("Any");
      card.setDetail(String.format(detail, genderRule, crr.getAgeRangeLow(),
          crr.getAgeRangeHigh(), patientStateRule, crr.getEquipmentCode(),
          providerStateRule));
    }
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
