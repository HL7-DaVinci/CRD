package org.hl7.davinci.cdshooks.orderreview;

import org.hl7.davinci.cdshooks.CdsRequest;
import javax.validation.constraints.NotNull;

public class CrdCdsRequest extends CdsRequest {
  @NotNull private OrderReviewContext context = null;
  @NotNull private OrderReviewPrefetch prefetch = null;

  @Override
  public OrderReviewContext getContext() {
    return context;
  }

  public void setContext(OrderReviewContext context) {
    this.context = context;
  }

  @Override
  public OrderReviewPrefetch getPrefetch() {
    return prefetch;
  }

  public void setPrefetch(OrderReviewPrefetch prefetch) {
    this.prefetch = prefetch;
  }
}
