package org.hl7.davinci;

public class PractitionerRoleInfo {
  private String locationAddressState;

  public PractitionerRoleInfo(String locationAddressState) {
    this.locationAddressState = locationAddressState;
  }

  public String getLocationAddressState() {
    return locationAddressState;
  }

  public void setLocationAddressState(String locationAddressState) {
    this.locationAddressState = locationAddressState;
  }
}
