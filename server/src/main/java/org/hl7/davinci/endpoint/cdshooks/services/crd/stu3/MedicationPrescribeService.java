package org.hl7.davinci.endpoint.cdshooks.services.crd.stu3;

import org.hl7.davinci.cdshooks.CdsService;
import org.hl7.davinci.cdshooks.Hook;
import org.hl7.davinci.cdshooks.Prefetch;
import org.hl7.davinci.cdshooks.stu3.CrdPrefetchTemplateElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component("stu3_MedicationPrescribeService")
public class MedicationPrescribeService extends CdsService {
  static final Logger logger = LoggerFactory.getLogger(MedicationPrescribeService.class);

  public static final String ID = "medication-prescribe-crd";
  public static final String TITLE = "medication-prescribe Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.MEDICATION_PRESCRIBE;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static Prefetch PREFETCH = null;
  static {
    PREFETCH = new Prefetch();
    PREFETCH.put(CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE.getKey(),
        CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE.getQuery());
  }

  public MedicationPrescribeService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH);
  }

}
