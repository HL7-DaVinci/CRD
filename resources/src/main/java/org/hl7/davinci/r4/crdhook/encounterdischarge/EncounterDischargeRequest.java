package org.hl7.davinci.r4.crdhook.encounterdischarge;

import org.cdshooks.CdsRequest;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;

import java.util.HashMap;

public class EncounterDischargeRequest extends CdsRequest<CrdPrefetch, EncounterDischargeContext> {

    private HashMap<String, Object> mapForPrefetchTemplates = null;

    @Override
    public Object getDataForPrefetchToken() {
        if (mapForPrefetchTemplates != null) {
            return mapForPrefetchTemplates;
        }
        mapForPrefetchTemplates = new HashMap<>();

        HashMap<String, Object> contextMap = new HashMap<>();
        contextMap.put("userId", getContext().getUserId());
        contextMap.put("patientId", getContext().getPatientId());
        contextMap.put("encounterId", getContext().getEncounterId());
        mapForPrefetchTemplates.put("context", contextMap);

        return mapForPrefetchTemplates;
    }

}
