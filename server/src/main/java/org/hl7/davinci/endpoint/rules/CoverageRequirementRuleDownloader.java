package org.hl7.davinci.endpoint.rules;

import org.hl7.davinci.endpoint.cql.bundle.CqlBundleFile;

public interface CoverageRequirementRuleDownloader {

  public CqlBundleFile downloadCqlBundleFile(Long id);
}
