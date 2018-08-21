package endpoint.components;

import endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.cdshooks.Card;
import org.hl7.davinci.cdshooks.Link;
import org.hl7.davinci.cdshooks.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience methods for working with CDS Hooks cards.
 */
public class CardBuilder {

  /**
   * Transforms a result from the database into a card.
   * @param crr coverage rule from the database
   * @return card with appropriate information
   */
  public static Card transform(CoverageRequirementRule crr) {
    Card card = baseCard();
    if (crr.getNoAuthNeeded()) {
      String summary = String.format("No documentation is required for a device or service with code: %s",
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
      String detail = "There are documentation requirements for someone who is %s and between the ages of "
          + "%d and %d for a device or service with code: %s.";
      card.setDetail(String.format(detail, crr.getGenderCode(), crr.getAgeRangeLow(),
          crr.getAgeRangeHigh(), crr.getEquipmentCode()));
    }
    return card;
  }

  /**
   * Creates a card with a summary but also has all of the necessary fields populated to be valid.
   * @param summary The desired summary for the card
   * @return valid card
   */
  public static Card summaryCard(String summary) {
    Card card = baseCard();
    card.setSummary(summary);
    return card;
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
