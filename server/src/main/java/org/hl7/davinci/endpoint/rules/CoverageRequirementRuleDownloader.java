package org.hl7.davinci.endpoint.rules;

import org.hl7.davinci.endpoint.cql.bundle.CqlBundleFile;

public interface CoverageRequirementRuleDownloader {

  public CqlBundleFile getFile(String payer, String codeSystem, String code, String name);

  public CqlBundleFile downloadCqlBundleFile(Long id, String name);
}
