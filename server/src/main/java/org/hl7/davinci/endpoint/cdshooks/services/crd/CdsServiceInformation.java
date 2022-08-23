package org.hl7.davinci.endpoint.cdshooks.services.crd;

import java.util.ArrayList;
import java.util.List;

public class CdsServiceInformation {
  private List<CdsAbstract> services = null;

  /**
   * Add a service.
   * @param servicesItem The service.
   * @return
   */
  public CdsServiceInformation addServicesItem(CdsAbstract servicesItem) {
    if (this.services == null) {
      this.services = new ArrayList<>();
    }
    this.services.add(servicesItem);
    return this;
  }

  public List<CdsAbstract> getServices() {
    return services;
  }

  public void setServices(List<CdsAbstract> services) {
    this.services = services;
  }
}
