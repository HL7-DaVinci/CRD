package org.cdshooks;

public class Source {
  private String label = null;

  private String url = null;

  private String icon = null;

  private Coding topic = null;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) { this.label = label; }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getIcon() { return icon; }

  public void setIcon(String icon) { this.icon = icon; }

  public Coding getTopic() { return topic; }

  public void setTopic(Coding topic) { this.topic = topic; }
}
