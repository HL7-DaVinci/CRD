package org.cdshooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Suggestion {
  private String label = null;

  private UUID uuid = null;

  private List<Action> actions = null;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
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
}
