package org.hl7.davinci.r4.crdhook.ordersign;

import org.hl7.davinci.r4.crdhook.ConfigurationOption;

public class CrdExtensionConfigurationOptions {

  public static final ConfigurationOption ALTERNATIVE_THERAPY = new ConfigurationOption(
      "alt-drug",
      "boolean",
      "Alternative therapy",
      "Provides recommendations for alternative therapy with equivalent/similar "
       + "clinical effect for which the patient has better coverage, that will incur lesser code",
      true
  );

}
