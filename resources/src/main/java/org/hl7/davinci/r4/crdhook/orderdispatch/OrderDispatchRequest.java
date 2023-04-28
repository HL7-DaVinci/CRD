package org.hl7.davinci.r4.crdhook.orderdispatch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cdshooks.CdsRequest;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;

import java.util.HashMap;

public class OrderDispatchRequest extends CdsRequest<CrdPrefetch, OrderDispatchContext> {
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

        HashMap<String, Object> contextMap = new HashMap<>();
        contextMap.put("patientId", getContext().getPatientId());
        contextMap.put("order", getContext().getOrder());
        contextMap.put("performer", getContext().getPerformer());
        contextMap.put("task", getContext().getTask());
        mapForPrefetchTemplates.put("context", contextMap);

        return mapForPrefetchTemplates;
    }

    @Override
    public String toString() {
        return "Super: " + super.toString() + " OrderDispatchRequest: " + getDataForPrefetchToken().toString();
    }
}
