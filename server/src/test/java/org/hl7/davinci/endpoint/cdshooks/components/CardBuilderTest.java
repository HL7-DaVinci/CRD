package org.hl7.davinci.endpoint.cdshooks.components;

import org.cdshooks.Card;
import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.components.CardBuilder.CqlResultsForCard;
import org.hl7.davinci.endpoint.config.YamlConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardBuilderTest
{
  @Test
  public void testRulesWithNoAuthNeeded()
  {
    CqlResultsForCard cardResults = new CardBuilder.CqlResultsForCard();
    cardResults.setRuleApplies(true);
    cardResults.setDetails("Some details.");
    cardResults.setInfoLink("http://some.link");
    cardResults.setSummary("The summary!");

    Card card = CardBuilder.transform(cardResults, new YamlConfig());
    assertEquals("The summary!", card.getSummary());
    assertEquals("Some details.", card.getDetail());
    assertEquals("http://some.link", card.getLinks().get(0).getUrl());
  }
}
