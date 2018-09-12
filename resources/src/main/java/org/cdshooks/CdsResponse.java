package org.cdshooks;

import java.util.ArrayList;
import java.util.List;

public class CdsResponse {

  /**
   * An array of Cards. Cards can provide a combination of information (for reading), suggested
   * actions (to be applied if a user selects them), and links (to launch an app if the user selects
   * them). The EHR decides how to display cards, but we recommend displaying suggestions using
   * buttons, and links using underlined text. REQUIRED
   */
  private List<Card> cards = null;

  /**
   * Add a card.
   * @param cardsItem The card.
   * @return
   */
  public CdsResponse addCard(Card cardsItem) {
    if (this.cards == null) {
      this.cards = new ArrayList<Card>();
    }
    this.cards.add(cardsItem);
    return this;
  }

  public List<Card> getCards() {
    return cards;
  }

  public void setCards(List<Card> cards) {
    this.cards = cards;
  }
}
