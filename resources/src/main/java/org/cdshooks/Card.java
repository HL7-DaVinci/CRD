package org.cdshooks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Card {
  private String uuid = null;
  private String summary = null;
  private String detail = null;
  private IndicatorEnum indicator = null;
  private Source source = null;
  private List<Suggestion> suggestions = null;
  private SelectionBehaviorEnum selectionBehavior = null;
  private List<Link> links = null;
  private CardExtension extension = null;

  public Card() { this.uuid = UUID.randomUUID().toString(); }

  public String getUuid() { return uuid; }

  private void setUuid(String uuid) { this.uuid = uuid; }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }
  
  // Add details to what already exists
  public void addDetail(String detail) {
    if (this.detail == null) {
      setDetail(detail);
    } else {
      this.detail = detail + "\n" + this.detail;
    }
  }

  public IndicatorEnum getIndicator() {
    return indicator;
  }

  public void setIndicator(IndicatorEnum indicator) {
    this.indicator = indicator;
  }

  public Source getSource() {
    return source;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  /**
   * Add a suggestion.
   * @param suggestionsItem The suggestion.
   * @return
   */
  public Card addSuggestionsItem(Suggestion suggestionsItem) {
    if (this.suggestions == null) {
      this.suggestions = new ArrayList<Suggestion>();
    }
    this.suggestions.add(suggestionsItem);
    return this;
  }

  public List<Suggestion> getSuggestions() {
    return suggestions;
  }

  public void setSuggestions(List<Suggestion> suggestions) {
    this.suggestions = suggestions;
  }

  public SelectionBehaviorEnum getSelectionBehavior() { return selectionBehavior; }

  public void setSelectionBehavior(SelectionBehaviorEnum selectionBehavior) { this.selectionBehavior = selectionBehavior; }

  /**
   * Add a link.
   * @param linksItem The link.
   * @return
   */
  public Card addLinksItem(Link linksItem) {
    if (this.links == null) {
      this.links = new ArrayList<Link>();
    }
    this.links.add(linksItem);
    return this;
  }

  public List<Link> getLinks() {
    return links;
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }

  public CardExtension getExtension() {
    return this.extension;
  }

  public void setExtension(CardExtension extension) {
    this.extension = extension;
  }

  public enum IndicatorEnum {
    INFO("info"),

    WARNING("warning"),

    HARD_STOP("hard-stop");

    private String value;

    IndicatorEnum(String value) {
      this.value = value;
    }

    /**
     * Create the enum value from a string. Needed because the values have illegal java chars.
     * @param value One of the enum values.
     * @return indicatorEnum
     */
    @JsonCreator
    public static IndicatorEnum fromValue(String value) throws IOException {
      for (IndicatorEnum indicatorEnum : IndicatorEnum.values()) {
        if (indicatorEnum.toString().equals(value)) {
          return indicatorEnum;
        }
      }
      return null;
      //    throw new RuntimeException("Hook was set to '" + value +
      //        "', but it can only be one of: " + Arrays.toString(Hook.values()));
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }

  public enum SelectionBehaviorEnum {
    ANY("any"),
    AT_MOST_ONE("at-most-one");

    private String value;

    SelectionBehaviorEnum(String value) { this.value = value; }

    /**
     * Create the enum value from a string. Needed because the values have illegal java chars.
     * @param value One of the enum values.
     * @return selectionBehaviorEnum
     */
    @JsonCreator
    public static SelectionBehaviorEnum fromValue(String value) throws IOException {
      for (SelectionBehaviorEnum selectionBehaviorEnum : SelectionBehaviorEnum.values()) {
        if (selectionBehaviorEnum.toString().equals(value)) {
          return selectionBehaviorEnum;
        }
      }
      return null;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
  }
}
