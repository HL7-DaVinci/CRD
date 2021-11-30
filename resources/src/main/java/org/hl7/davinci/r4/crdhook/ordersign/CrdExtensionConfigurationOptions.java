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
      new Configuration().getAlternativeTherapy()
  );
}
