package org.hl7.davinci.cdshooks.orderreview;

import org.hl7.davinci.cdshooks.CdsRequest;
import org.hl7.davinci.cdshooks.CrdPrefetch;

import javax.validation.constraints.NotNull;

public class OrderReviewRequest extends CdsRequest {
  @NotNull
  private OrderReviewContext context = null;

  @Override
  public OrderReviewContext getContext() {
    return context;
  }

  public void setContext(OrderReviewContext context) {
    this.context = context;
  }

}
