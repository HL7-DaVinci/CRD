package org.hl7.davinci.r4.crdhook.appointmentbook;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cdshooks.CdsRequest;
import org.hl7.davinci.r4.Utilities;
import org.hl7.davinci.r4.crdhook.CrdPrefetch;

public class AppointmentBookRequest extends CdsRequest<CrdPrefetch, AppointmentBookContext> {


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
		//contextMap.put("appointments", Utilities.bundleAsHashmap(getContext().getAppointments()));
	   //   contextMap.put("draftOrders", Utilities.bundleAsHashmap(getContext().getDraftOrders()));
	      mapForPrefetchTemplates.put("context", contextMap);

        try {
            System.out.println(new ObjectMapper().writeValueAsString(mapForPrefetchTemplates));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        return mapForPrefetchTemplates;
	}

}
