package org.hl7.davinci.stu3.crdhook.orderreview;

import org.cdshooks.CdsRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import org.hl7.davinci.stu3.Utilities;
import org.hl7.davinci.stu3.crdhook.CrdPrefetch;

public class OrderReviewRequest extends CdsRequest<OrderReviewContext, CrdPrefetch> {

  private HashMap<String, Object> mapForPrefetchTemplates = null;

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

