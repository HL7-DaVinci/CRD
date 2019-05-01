package org.hl7.davinci.ehrserver.authproxy;

public class AppContext {

  private String template;
  private String request;


  public String getRequest() {
    return request;
  }

  public String getTemplate() {
    return template;
  }

  public void setRequest(String request) {
    this.request = request;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  @Override
  public String toString() {
    return template + ", "  + request;
  }
}
