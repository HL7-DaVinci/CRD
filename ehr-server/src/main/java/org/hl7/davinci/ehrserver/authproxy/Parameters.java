package org.hl7.davinci.ehrserver.authproxy;

public class Parameters {
  private String patientId;
  private String appContext;


  public String getAppContext() {
    return appContext;
  }

  public String getPatientId() {
    return patientId;
  }

  public void setAppContext(String appContext) {
    this.appContext = appContext;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  @Override
  public String toString() {
    return patientId + ", "  + appContext.toString();
  }
}
