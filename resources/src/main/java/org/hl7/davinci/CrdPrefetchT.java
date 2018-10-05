package org.hl7.davinci;


import org.hl7.fhir.instance.model.api.IBaseBundle;

public interface CrdPrefetchT<BundleT extends IBaseBundle> {

  BundleT getDeviceRequestBundle();

  void setDeviceRequestBundle(BundleT deviceRequestBundle);

  BundleT getMedicationRequestBundle();

  void setMedicationRequestBundle(BundleT medicationRequestBundle);

  BundleT getNutritionOrderBundle();

  void setNutritionOrderBundle(BundleT nutritionOrderBundle);

  BundleT getSupplyRequestBundle();

  void setSupplyRequestBundle(BundleT supplyRequestBundle);

}
