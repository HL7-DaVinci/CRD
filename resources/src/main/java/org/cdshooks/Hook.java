package org.cdshooks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.IOException;

public enum Hook {

  ORDER_REVIEW("order-review"), MEDICATION_PRESCRIBE("medication-prescribe");

  private String value;

  Hook(String value) {
    this.value = value;
  }

  /**
   * Create the enum value from a string. Needed because the values have illegal java chars.
   * @param value One of the enum values.
   * @return Hook
   */
  @JsonCreator
  public static Hook fromValue(String value) throws IOException {
    for (Hook hook : Hook.values()) {
      if (hook.toString().equals(value)) {
        return hook;
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
