package org.hl7.davinci.r4;

import org.cdshooks.Coding;

public enum CardTypes {
  COVERAGE("coverage", "Coverage"),
  DOCUMENTATION("documentation", "Documentation"),
  PRIOR_AUTH("prior-auth", "Prior Authorization"),
  DTR_CLIN("dtr-clin", "DTR Clin"),
  DTR_ADMIN("dtr-admin", "DTR Admin"),
  CLAIM("claim", "Claim"),
  INSURANCE("insurance", "Insurance"),
  LIMITS("limits", "Limits"),
  NETWORK("network", "Network"),
  APPROPRIATE_USE("appropriate-use", "Appropriate Use"),
  COST("cost", "Cost"),
  THERAPY_ALTERNATIVES_OPT("therapy-alternatives-opt", "Therapy Alternatives Opt"),
  THERAPY_ALTERNATIVES_REG("therapy-alternatives-req", "Therapy Alternatives Req"),
  CLINICAL_REMINDER("clinical-reminder", "Clinical Reminder"),
  DUPLICATE_THERAPY("duplicate-therapy", "Duplicate Therapy"),
  CONTRAINDICATION("contraindication", "Contraindication"),
  GUIDELINE("guideline", "Guideline"),
  OFF_GUIDELINE("off-guideline", "Off Guideline");

  private String code;
  private String display;
  private static String codeSystem = "http://hl7.org/fhir/us/davinci-crd/CodeSystem/cardType";

  CardTypes(String code, String display) {
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
