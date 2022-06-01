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
    return this.bundleContainsResourceId(this.deviceRequestBundle, id)
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
    StringBuilder sb = new StringBuilder();
    List<BundleEntryComponent> entries = new ArrayList<>();
    if(this.deviceRequestBundle != null){
      entries = this.deviceRequestBundle.getEntry();
    } else if(this.nutritionOrderBundle != null){
      entries = this.nutritionOrderBundle.getEntry();
    } else if(this.serviceRequestBundle != null){
      entries = this.serviceRequestBundle.getEntry();
    } else if(this.medicationDispenseBundle != null){
      entries = this.medicationDispenseBundle.getEntry();
    } else if(this.medicationStatementBundle != null){
      entries = this.medicationStatementBundle.getEntry();
    } else if(this.encounterBundle != null){
      entries = this.encounterBundle.getEntry();
    } else if(this.appointmentBundle != null){
      entries = this.appointmentBundle.getEntry();
    } else if(this.medicationRequestBundle != null){
      entries = this.medicationRequestBundle.getEntry();
    } else if(this.supplyRequestBundle != null){
      entries = this.supplyRequestBundle.getEntry();
    }
    sb.append("[");
    for(BundleEntryComponent entry : entries) {
      sb.append(entry.getResource());
      sb.append("-");
      sb.append(entry.getResource().getId());
      sb.append(",");
    }
    sb.setLength(sb.length()-1);
    sb.append("]");
    return sb.toString();
  }
}
