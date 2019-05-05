package org.cdshooks;

import org.json.simple.JSONObject;

public class Link {
  private String label = null;

  private String url = null;

  private String type = null;

  private String appContext = null;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  // might need to be escaped json instead of an actual json object
  public String getAppContext() {
    return appContext;
  }

  public void setAppContext(String appContext) {
    this.appContext = appContext;
  }
}
