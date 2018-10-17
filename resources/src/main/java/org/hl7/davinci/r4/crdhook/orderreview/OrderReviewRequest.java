package org.hl7.davinci.r4.crdhook.orderreview;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cdshooks.CdsRequest;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;

import java.util.HashMap;

public class OrderReviewRequest extends CdsRequest<CrdPrefetch, OrderReviewContext> {

  private HashMap<String, Object> mapForPrefetchTemplates = null;

  /**
   * Gets the data from the context to put into the prefetch token.
   * @return a map of prefetch attributes to their values
   */
  @JsonIgnore
  public Object getDataForPrefetchToken() {
    if (mapForPrefetchTemplates != null) {
      return mapForPrefetchTemplates;
    }
    mapForPrefetchTemplates = new HashMap<>();
    mapForPrefetchTemplates.put("user", this.getUser());

    HashMap<String, Object> contextMap = new HashMap<>();
    contextMap.put("patientId", getContext().getPatientId());
    contextMap.put("encounterId", getContext().getEncounterId());
    contextMap.put("orders", Utilities.bundleAsHashmap(getContext().getOrders()));
    mapForPrefetchTemplates.put("context", contextMap);

    return mapForPrefetchTemplates;
  }


}
