package org.hl7.davinci.cdshooks.orderreview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.hl7.davinci.Utilities;
import org.hl7.davinci.cdshooks.CdsRequest;
import org.hl7.davinci.cdshooks.CrdPrefetch;

import javax.validation.constraints.NotNull;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;

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

  private HashMap<String, Object> mapForPrefetchTemplates = null;

  public Object getDataForPrefetchToken() {
    if (mapForPrefetchTemplates != null) {
      return mapForPrefetchTemplates;
    }
    mapForPrefetchTemplates = new HashMap<>();
    mapForPrefetchTemplates.put("user", this.getUser());

    HashMap<String, Object> contextMap = new HashMap<>();
    contextMap.put("patientId",getContext().getPatientId());
    contextMap.put("encounterId",getContext().getEncounterId());
    contextMap.put("orders",Utilities.bundleAsHashmap(getContext().getOrders()));
    mapForPrefetchTemplates.put("context", contextMap);

    return mapForPrefetchTemplates;
  }


}
