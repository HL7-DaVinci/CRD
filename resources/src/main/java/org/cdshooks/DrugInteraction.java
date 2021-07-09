package org.cdshooks;

public class DrugInteraction {
  private boolean applies;
  private String summary;
  private String detail;

  public boolean getApplies() { return applies; }

  public DrugInteraction setApplies(boolean applies) {
    this.applies = applies;
    return this;
  }

  public String getSummary() { return summary; }

  public DrugInteraction setSummary(String summary) {
    this.summary = summary;
    return this;
  }

  public String getDetail() { return detail; }

  public DrugInteraction setDetail(String detail) {
    this.detail = detail;
    return this;
  }

  public String toString() { return summary + "(" + detail + ")"; }
}
