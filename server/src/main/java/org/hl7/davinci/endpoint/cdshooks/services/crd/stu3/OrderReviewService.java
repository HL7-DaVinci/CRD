package org.hl7.davinci.endpoint.cdshooks.services.crd.stu3;

import java.util.Arrays;
import java.util.List;
import org.hl7.davinci.cdshooks.CdsService;
import org.hl7.davinci.cdshooks.Hook;
import org.hl7.davinci.cdshooks.Prefetch;
import org.hl7.davinci.cdshooks.stu3.CrdPrefetchTemplateElements;
import org.hl7.davinci.cdshooks.stu3.CrdPrefetchTemplateElements.PrefetchTemplateElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component("stu3_OrderReviewService")
public class OrderReviewService extends CdsService {

  static final Logger logger = LoggerFactory.getLogger(OrderReviewService.class);

  public static final String ID = "order-review-crd";
  public static final String TITLE = "order-review Coverage Requirements Discovery";
  public static final Hook HOOK = Hook.ORDER_REVIEW;
  public static final String DESCRIPTION =
      "Get information regarding the coverage requirements for durable medical equipment";
  public static Prefetch PREFETCH = null;
  static {
    PREFETCH = new Prefetch();
    List<PrefetchTemplateElement> elements = Arrays.asList(
        CrdPrefetchTemplateElements.DEVICE_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.SUPPLY_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.NUTRITION_ORDER_BUNDLE,
        CrdPrefetchTemplateElements.MEDICATION_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.PROCEDURE_REQUEST_BUNDLE,
        CrdPrefetchTemplateElements.REFERRAL_REQUEST_BUNDLE
    );
    for (PrefetchTemplateElement element : elements) {
      PREFETCH.put(element.getKey(), element.getQuery());
    }
  }

  public OrderReviewService() {
    super(ID, HOOK, TITLE, DESCRIPTION, PREFETCH);
  }

}
