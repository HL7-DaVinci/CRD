package org.hl7.davinci.cdshooks.orderreview;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

public class OrderReviewFetcherTest {
  @Test
  public void testCompareReferenceToId() {
    assertTrue(OrderReviewFetcher.compareReferenceToId("Patient/1234","1234"));
    assertTrue(OrderReviewFetcher.compareReferenceToId("1234","Patient/1234"));
    assertTrue(OrderReviewFetcher.compareReferenceToId("1234","1234"));
    assertTrue(OrderReviewFetcher.compareReferenceToId("Patient/1234","Patient/1234"));

    assertFalse(OrderReviewFetcher.compareReferenceToId("Patient/1234","4321"));
    assertFalse(OrderReviewFetcher.compareReferenceToId("1234","Patient/4321"));
    assertFalse(OrderReviewFetcher.compareReferenceToId("1234","4321"));
    assertFalse(OrderReviewFetcher.compareReferenceToId("Patient/1234","Patient/4321"));

    assertFalse(OrderReviewFetcher.compareReferenceToId("Patient/1234","Practitioner/1234"));
    assertFalse(OrderReviewFetcher.compareReferenceToId("Patient/1234","Practitioner/4321"));
  }
}
