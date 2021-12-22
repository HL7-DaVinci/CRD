package org.cdshooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Suggestion {
  private String label = null;

  private String uuid = null;

  private List<Action> actions = null;

  private boolean isRecommended = true;

  public Suggestion() { this.uuid = UUID.randomUUID().toString(); }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getUuid() { return uuid; }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * Add an action.
   * @param actionsItem The action.
   * @return
   */
  public Suggestion addActionsItem(Action actionsItem) {
    if (this.actions == null) {
      this.actions = new ArrayList<>();
    }
    this.actions.add(actionsItem);
    return this;
  }

  public List<Action> getActions() {
    return actions;
  }

  public void setActions(List<Action> actions) {
    this.actions = actions;
  }

  public boolean getIsRecommended() { return isRecommended; }

  public void setIsRecommended(boolean isRecommended) { this.isRecommended = isRecommended; }
}
