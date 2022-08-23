package org.hl7.davinci.r4.crdhook;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.hl7.davinci.PrefetchTemplateElement;
import org.hl7.davinci.r4.JacksonCrdPrefetchDeserializer;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

/**
 * Class that supports the representation of prefetch information in a CDS Hook request.
 * It appears that for CRD, prefetch information will be the same, regardless of hook type.
 */
@JsonDeserialize(using = JacksonCrdPrefetchDeserializer.class)
public class CrdPrefetch {

  private Bundle coverageBundle;
  private Bundle deviceRequestBundle;
  private Bundle medicationRequestBundle;
  private Bundle nutritionOrderBundle;
  private Bundle serviceRequestBundle;
  private Bundle supplyRequestBundle;
  private Bundle appointmentBundle;
  private Bundle encounterBundle;
  private Bundle medicationDispenseBundle;
  private Bundle medicationStatementBundle;
  // The list of prefetch queries to execute.
  private List<PrefetchTemplateElement> prefetchQueries;

  public CrdPrefetch(){
    this.prefetchQueries = new ArrayList<>();
  }

  public List<PrefetchTemplateElement> getAdditionalPrefetchQueries() {
    return this.prefetchQueries;
  }

  public Bundle getCoverageBundle() {
    if (coverageBundle == null) {
      this.coverageBundle = new Bundle();
    }
    return coverageBundle;
  }

  public void setCoverageBundle(Bundle coverageBundle) {
    this.coverageBundle = coverageBundle;
  }

  public Bundle getDeviceRequestBundle() {
    return deviceRequestBundle;
  }

  public void setDeviceRequestBundle(Bundle deviceRequestBundle) {
    this.deviceRequestBundle = deviceRequestBundle;
  }

  public Bundle getMedicationRequestBundle() {
    return medicationRequestBundle;
  }

  public void setMedicationRequestBundle(Bundle medicationRequestBundle) { this.medicationRequestBundle = medicationRequestBundle; }

  public Bundle getMedicationDispenseBundle() {
    return medicationDispenseBundle;
  }

  public void setMedicationDispenseBundle(Bundle medicationDispenseBundle) { this.medicationDispenseBundle = medicationDispenseBundle; }

  public Bundle getNutritionOrderBundle() {
    return nutritionOrderBundle;
  }

  public void setNutritionOrderBundle(Bundle nutritionOrderBundle) {
    this.nutritionOrderBundle = nutritionOrderBundle;
  }

  public Bundle getServiceRequestBundle() {
    return serviceRequestBundle;
  }

  public void setServiceRequestBundle(Bundle serviceRequestBundle) {
    this.serviceRequestBundle = serviceRequestBundle;
  }

  public Bundle getSupplyRequestBundle() {
    return supplyRequestBundle;
  }

  public void setSupplyRequestBundle(Bundle supplyRequestBundle) {
    this.supplyRequestBundle = supplyRequestBundle;
  }

  public Bundle getAppointmentBundle() { return appointmentBundle; }

  public void setAppointmentBundle(Bundle appointmentBundle) { this.appointmentBundle = appointmentBundle; }

  public Bundle getEncounterBundle() { return encounterBundle; }

  public void setEncounterBundle(Bundle encounterBundle) { this.encounterBundle = encounterBundle; }

  public Bundle getMedicationStatementBundle() { return medicationStatementBundle; }

  public void setMedicationStatementBundle(Bundle medicationStatementBundle) { this.medicationStatementBundle = medicationStatementBundle; }

  /**
   * Checks whether the given resource exists in the requested resource type.
   * @param id
   * @return
   */
  public boolean containsRequestResourceId(String id) {
    return bundleContainsResourceId(this.coverageBundle, id)
        || bundleContainsResourceId(this.deviceRequestBundle, id)
        || bundleContainsResourceId(this.medicationRequestBundle, id)
        || bundleContainsResourceId(this.nutritionOrderBundle, id)
        || bundleContainsResourceId(this.serviceRequestBundle, id)
        || bundleContainsResourceId(this.supplyRequestBundle, id)
        || bundleContainsResourceId(this.appointmentBundle, id)
        || bundleContainsResourceId(this.encounterBundle, id)
        || bundleContainsResourceId(this.medicationDispenseBundle, id)
        || bundleContainsResourceId(this.medicationStatementBundle, id);
  }

  /**
   * Returns whether the given bundle contains the given resource.
   * @param bundle
   * @param id
   * @return
   */
  private static boolean bundleContainsResourceId(Bundle bundle, String id) {
    if (bundle == null) {
      return false;
    }
    if (id.contains("/")) {
      String[] splitId = id.split("/");
      id = splitId[splitId.length-1];
    }
    final String idToCheck = id;
    return bundle.getEntry().stream()
        .anyMatch(entry -> entry.getResource().getId().contains(idToCheck));
  }

  @Override
  public String toString() {

    StringBuilder sb = new StringBuilder();
    sb.append("[");
    BiConsumer<String, Bundle> bundlesPrinter = (key, bundle) -> {
      sb.append(key).append(":{");
      for(BundleEntryComponent entry : bundle.getEntry()) {
        sb.append(entry.getResource());
        sb.append("~");
        sb.append(entry.getResource().getId());
        sb.append(",");
      }
      sb.append("}");
    };

    if(this.deviceRequestBundle != null) {
      bundlesPrinter.accept("deviceRequestBundle", deviceRequestBundle);
    } if(this.nutritionOrderBundle != null){
      bundlesPrinter.accept("nutritionOrderBundle", nutritionOrderBundle);
    } if(this.serviceRequestBundle != null){
      bundlesPrinter.accept("serviceRequestBundle", serviceRequestBundle);
    } if(this.medicationDispenseBundle != null){
      bundlesPrinter.accept("medicationDispenseBundle", medicationDispenseBundle);
    } if(this.medicationStatementBundle != null){
      bundlesPrinter.accept("medicationStatementBundle", medicationStatementBundle);
    } if(this.encounterBundle != null){
      bundlesPrinter.accept("encounterBundle", encounterBundle);
    } if(this.appointmentBundle != null){
      bundlesPrinter.accept("appointmentBundle", appointmentBundle);
    } if(this.medicationRequestBundle != null){
      bundlesPrinter.accept("medicationRequestBundle", medicationRequestBundle);
    } if(this.supplyRequestBundle != null){
      bundlesPrinter.accept("supplyRequestBundle", supplyRequestBundle);
    } if(this.coverageBundle != null) {
      bundlesPrinter.accept("coverageBundle", coverageBundle);
    }

    sb.append("]");
    return sb.toString();
  }

  /**
   * Adds the given prefetch query to the list of prefetch queries to execute.
   * @param prefetchKey
   * @param prefetchQuery
   */
  public void addPrefetchQuery(String prefetchKey, String prefetchQuery) {
    PrefetchTemplateElement prefetchTemplate = new PrefetchTemplateElement(prefetchKey, Bundle.class, prefetchQuery.replaceAll("\"", ""));
    this.prefetchQueries.add(prefetchTemplate);
  }

}
