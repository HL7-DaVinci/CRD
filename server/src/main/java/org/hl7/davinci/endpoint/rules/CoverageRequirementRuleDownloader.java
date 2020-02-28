package org.hl7.davinci.endpoint.rules;

import org.hl7.davinci.endpoint.files.FileResource;

public interface CoverageRequirementRuleDownloader {

  public FileResource getFile(String payer, String codeSystem, String code, String name);
}
