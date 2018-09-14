package org.cdshooks;

import java.util.ArrayList;
import java.util.List;

public class CdsServiceInformation {
  private List<CdsService> services = null;

  /**
   * Add a service.
   * @param servicesItem The service.
   * @return
   */
  public CdsServiceInformation addServicesItem(CdsService servicesItem) {
    if (this.services == null) {
      this.services = new ArrayList<CdsService>();
    }
    this.services.add(servicesItem);
    return this;
  }

  public List<CdsService> getServices() {
    return services;
  }

  public void setServices(List<CdsService> services) {
    this.services = services;
  }
}
