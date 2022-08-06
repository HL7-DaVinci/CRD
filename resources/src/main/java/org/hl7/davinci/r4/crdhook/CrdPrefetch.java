package org.hl7.davinci.r4.crdhook;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hl7.davinci.r4.JacksonBundleDeserializer;
import org.hl7.davinci.r4.JacksonHapiSerializer;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

/**
 * Class that supports the representation of prefetch information in a CDS Hook request.
 * It appears that for CRD, prefetch information will be the same, regardless of hook type.
 */
public class CrdPrefetch {

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle coverageBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle deviceRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle medicationRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle nutritionOrderBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle serviceRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle supplyRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle appointmentBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle encounterBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle medicationDispenseBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonBundleDeserializer.class)
  private Bundle medicationStatementBundle;

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
    return this.bundleContainsResourceId(this.coverageBundle, id)
        || this.bundleContainsResourceId(this.deviceRequestBundle, id)
        || this.bundleContainsResourceId(this.medicationRequestBundle, id)
        || this.bundleContainsResourceId(this.nutritionOrderBundle, id)
        || this.bundleContainsResourceId(this.serviceRequestBundle, id)
        || this.bundleContainsResourceId(this.supplyRequestBundle, id)
        || this.bundleContainsResourceId(this.appointmentBundle, id)
        || this.bundleContainsResourceId(this.encounterBundle, id)
        || this.bundleContainsResourceId(this.medicationDispenseBundle, id)
        || this.bundleContainsResourceId(this.medicationStatementBundle, id);
  }

  /**
   * Returns whether the given bundle contains the given resource.
   * @param bundle
   * @param id
   * @return
   */
  private boolean bundleContainsResourceId(Bundle bundle, String id) {
    if (bundle == null) {
      return false;
    }
    if (id.contains("/")) {
      String[] splitId = id.split("/");
      id = splitId[splitId.length-1];
    }
    final String idToCheck = id;
    return bundle.getEntry().stream().anyMatch(entry -> entry.getResource().getId().contains(idToCheck));
  }

  @Override
  public String toString() {
    List<BundleEntryComponent> entries = new ArrayList<>();
    if(this.deviceRequestBundle != null) {
    entries.addAll(this.deviceRequestBundle.getEntry());
    } if(this.nutritionOrderBundle != null){
      entries.addAll(this.nutritionOrderBundle.getEntry());
    } if(this.serviceRequestBundle != null){
      entries.addAll(this.serviceRequestBundle.getEntry());
    } if(this.medicationDispenseBundle != null){
      entries.addAll(this.medicationDispenseBundle.getEntry());
    } if(this.medicationStatementBundle != null){
      entries.addAll(this.medicationStatementBundle.getEntry());
    } if(this.encounterBundle != null){
      entries.addAll(this.encounterBundle.getEntry());
    } if(this.appointmentBundle != null){
      entries.addAll(this.appointmentBundle.getEntry());
    } if(this.medicationRequestBundle != null){
      entries.addAll(this.medicationRequestBundle.getEntry());
    } if(this.supplyRequestBundle != null){
      entries.addAll(this.supplyRequestBundle.getEntry());
    } if(this.coverageBundle != null) {
      entries.addAll(this.coverageBundle.getEntry());
    }
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for(BundleEntryComponent entry : entries) {
      sb.append(entry.getResource());
      sb.append("~");
      sb.append(entry.getResource().getId());
      sb.append(",");
    }
    sb.setLength(sb.length()-1);
    sb.append("]");
    return sb.toString();
  }
}
