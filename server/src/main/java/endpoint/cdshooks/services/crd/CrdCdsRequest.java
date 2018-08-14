package endpoint.cdshooks.services.crd;

import endpoint.cdshooks.models.CdsRequest;
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
