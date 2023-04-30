package org.hl7.davinci.r4.crdhook.ordersign;

import org.cdshooks.Configuration;
import org.hl7.davinci.r4.crdhook.ConfigurationOption;

/*
 * The ConfigurationOptions defined here must match the Configuration in
 * Configuration.java.
 */
public class CrdExtensionConfigurationOptions {
  public static final ConfigurationOption ALTERNATIVE_THERAPY = new ConfigurationOption(
      "alt-drug",
      "boolean",
      "Alternative therapy",
      "Provides recommendations for alternative therapy with equivalent/similar "
       + "clinical effect for which the patient has better coverage, that will incur lesser code",
      new Configuration().getAlternativeTherapy().toString()
  );

  public static final ConfigurationOption DTR_CLIN = new ConfigurationOption(
          "dtr-clin",
          "boolean",
          "DTR Clin",
          "Indication that DTR is relevant for prior authorization, claim or other documentation requirements and that at least some clinical information needs to be captured",
          new Configuration().getDTRClin().toString()
  );

  public static final ConfigurationOption PRIOR_AUTH = new ConfigurationOption(
          "prior-auth",
          "boolean",
          "Prior Auth",
          "Provides indications of whether prior authorization is required for the proposed order",
          new Configuration().getPriorAuth().toString()
  );

  public static final ConfigurationOption COVERAGE = new ConfigurationOption(
          "coverage",
          "boolean",
          "Coverage",
          "Provides indications of whether coverage is required for the proposed order",
          new Configuration().getPriorAuth().toString()
  );

  public static final ConfigurationOption MAX_CARDS = new ConfigurationOption(
          "max-cards",
          "integer",
          "Maximum cards",
          "Indicates the maximum number of cards to be returned from the service.  The services will prioritize cards such as highest priority ones are delivered",
          String.valueOf(new Configuration().getMaxCards())
  );
}
