package org.hl7.davinci.endpoint.cdshooks.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.hl7.davinci.endpoint.components.CardBuilder;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.cdshooks.Card;
import org.junit.jupiter.api.Test;

public class CardBuilderTest {
  @Test
  public void testRulesWithNoAuthNeeded() {
    CoverageRequirementRule crr = new CoverageRequirementRule();
    crr.setAgeRangeHigh(80);
    crr.setAgeRangeLow(55);
    crr.setEquipmentCode("E0424");
    crr.setGenderCode("F".charAt(0));
    crr.setNoAuthNeeded(true);
    Card card = CardBuilder.transform(crr);
    assertEquals("No documentation is required for a device or service with code: E0424", card.getSummary());
    assertNull(card.getLinks());
  }

  @Test
  public void testRulesWithAuthNeeded() {
    CoverageRequirementRule crr = new CoverageRequirementRule();
    crr.setAgeRangeHigh(80);
    crr.setAgeRangeLow(55);
    crr.setEquipmentCode("E0424");
    crr.setGenderCode("F".charAt(0));
    crr.setNoAuthNeeded(false);
    crr.setInfoLink("http://www.mitre.org");
    Card card = CardBuilder.transform(crr);
    assertEquals("Documentation is required for the desired device or service", card.getSummary());
    assertEquals(1, card.getLinks().size());
    assertEquals("http://www.mitre.org", card.getLinks().get(0).getUrl());
  }
}
