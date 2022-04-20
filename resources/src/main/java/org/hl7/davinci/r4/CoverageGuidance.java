package org.hl7.davinci.r4;

import org.hl7.fhir.r4.model.Coding;

public enum CoverageGuidance {
  NOT_COVERED("not-covered", "Not Covered"),
  COVERED("covered", "Covered"),
  PRIOR_AUTH("prior-auth", "Prior authorization"),
  CLINICAL("clinical", "Clinical"),
  ADMIN("admin", "Admin");


  private String code;
  private String display;
  private static String codeSystem = "http://hl7.org/fhir/us/davinci-crd/CodeSystem/coverageGuidance";

  CoverageGuidance(String code, String display) {
    this.code = code;
    this.display = display;
  }

  public String getCode() { return code; }
  public String getDisplay() { return display; }
  public String getCodeSystem() { return codeSystem; }

  public Coding getCoding() {
    Coding coding = new Coding();
    coding.setSystem(codeSystem);
    coding.setCode(code);
    coding.setDisplay(display);
    return coding;
  }
}
