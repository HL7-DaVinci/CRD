package org.hl7.davinci.r4.crdhook.orderselect;

import org.cdshooks.Configuration;
import org.hl7.davinci.r4.crdhook.ConfigurationOption;

public class CrdExtensionConfigurationOptions {
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
