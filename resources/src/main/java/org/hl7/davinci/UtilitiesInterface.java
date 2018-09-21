package org.hl7.davinci;

import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseBundle;

import java.util.List;

public abstract class UtilitiesInterface<resourceTypeT extends IAnyResource,
    bundleTypeT extends IBaseBundle> {

  // implement
  public abstract <T extends resourceTypeT> List<T> getResourcesOfTypeFromBundle(Class<T> type, bundleTypeT bundle);


}
