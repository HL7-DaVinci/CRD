package org.hl7.davinci.ehrserver.authproxy;

public class AppContext {

  private String template;
  private String request;
  private String filepath;

  public String getRequest() {
    return request;
  }

  public String getTemplate() {
    return template;
  }

  public String getFilepath() { return filepath; }

  public void setRequest(String request) {
    this.request = request;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public void setFilepath(String filepath) {
    this.filepath = filepath;
  }

  @Override
  public String toString() {
    return "template=" + template + "&request=" + request + "&filepath=" + filepath;
  }
}
