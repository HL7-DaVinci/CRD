package org.hl7.davinci.endpoint.cdshooks.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.cdshooks.CoverageRequirements;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.CardBuilder.CqlResultsForCard;
import org.cdshooks.Card;
import org.hl7.davinci.r4.CardTypes;
import org.junit.jupiter.api.Test;

public class CardBuilderTest {
  @Test
  public void testRulesWithNoAuthNeeded() {
    CqlResultsForCard cardResults = new CardBuilder.CqlResultsForCard();
    CoverageRequirements coverageRequirements = new CoverageRequirements();
    cardResults.setRuleApplies(true);
    coverageRequirements.setApplies(true);
    coverageRequirements.setDetails("Some details.");
    coverageRequirements.setInfoLink("http://some.link");
    coverageRequirements.setSummary("The summary!");
    cardResults.setCoverageRequirements(coverageRequirements);
    CardBuilder cardBuilder = new CardBuilder();
    Card card = cardBuilder.transform(CardTypes.COVERAGE, cardResults);
    assertEquals("The summary!", card.getSummary());
    assertEquals("Some details.", card.getDetail());
    assertEquals("http://some.link", card.getLinks().get(0).getUrl());
  }
}
