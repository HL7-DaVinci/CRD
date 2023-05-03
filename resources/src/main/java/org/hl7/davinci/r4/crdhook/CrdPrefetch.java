package org.hl7.davinci.r4.crdhook;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hl7.davinci.r4.JacksonBundleDeserializer;
import org.hl7.davinci.r4.JacksonHapiSerializer;
import org.hl7.davinci.r4.JacksonIBaseResourceDeserializer;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

/**
 * Class that supports the representation of prefetch information in a CDS Hook request.
 * It appears that for CRD, prefetch information will be the same, regardless of hook type.
 */
public class CrdPrefetch {

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonIBaseResourceDeserializer.class)
  private IBaseResource coverageBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonIBaseResourceDeserializer.class)
  private IBaseResource deviceRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonIBaseResourceDeserializer.class)
  private IBaseResource medicationRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonIBaseResourceDeserializer.class)
  private IBaseResource nutritionOrderBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonIBaseResourceDeserializer.class)
  private IBaseResource serviceRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonIBaseResourceDeserializer.class)
  private IBaseResource supplyRequestBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonIBaseResourceDeserializer.class)
  private IBaseResource appointmentBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonIBaseResourceDeserializer.class)
  private IBaseResource encounterBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonIBaseResourceDeserializer.class)
  private IBaseResource medicationDispenseBundle;

  @JsonSerialize(using = JacksonHapiSerializer.class)
  @JsonDeserialize(using = JacksonIBaseResourceDeserializer.class)
  private IBaseResource medicationStatementBundle;

  public IBaseResource getCoverageBundle() {
    if (coverageBundle == null) {
      this.coverageBundle = new Bundle();
    }

    if (coverageBundle.getClass() != Bundle.class) {
      return null;
    }

    return coverageBundle;
  }

  public void setCoverageBundle(IBaseResource coverageBundle) {
    this.coverageBundle = coverageBundle;
  }

  public IBaseResource getDeviceRequestBundle() {
    return deviceRequestBundle;
  }

  public void setDeviceRequestBundle(IBaseResource deviceRequestBundle) {
    this.deviceRequestBundle = deviceRequestBundle;
  }

  public IBaseResource getMedicationRequestBundle() {
    return medicationRequestBundle;
  }

  public void setMedicationRequestBundle(IBaseResource medicationRequestBundle) { this.medicationRequestBundle = medicationRequestBundle; }

  public IBaseResource getMedicationDispenseBundle() {
    return medicationDispenseBundle;
  }

  public void setMedicationDispenseBundle(IBaseResource medicationDispenseBundle) { this.medicationDispenseBundle = medicationDispenseBundle; }

  public IBaseResource getNutritionOrderBundle() {
    return nutritionOrderBundle;
  }

  public void setNutritionOrderBundle(IBaseResource nutritionOrderBundle) {
    this.nutritionOrderBundle = nutritionOrderBundle;
  }

  public IBaseResource getServiceRequestBundle() {
    return serviceRequestBundle;
  }

  public void setServiceRequestBundle(IBaseResource serviceRequestBundle) {
    this.serviceRequestBundle = serviceRequestBundle;
  }

  public IBaseResource getSupplyRequestBundle() {
    return supplyRequestBundle;
  }

  public void setSupplyRequestBundle(IBaseResource supplyRequestBundle) {
    this.supplyRequestBundle = supplyRequestBundle;
  }

  public IBaseResource getAppointmentBundle() {
    return appointmentBundle;
  }

  public void setAppointmentBundle(IBaseResource appointmentBundle) { this.appointmentBundle = appointmentBundle; }

  public IBaseResource getEncounterBundle() {
    return encounterBundle;
  }

  public void setEncounterBundle(IBaseResource encounterBundle) { this.encounterBundle = encounterBundle; }

  public IBaseResource getMedicationStatementBundle() {
    return medicationStatementBundle; }

  public void setMedicationStatementBundle(IBaseResource medicationStatementBundle) { this.medicationStatementBundle = medicationStatementBundle; }

  /**
   * Checks whether the given resource exists in the requested resource type.
   * @param id
   * @return
   */
  public boolean containsRequestResourceId(String id) {
    return this.bundleContainsResourceId(this.getCoverageBundle(), id)
        || this.bundleContainsResourceId(this.getDeviceRequestBundle(), id)
        || this.bundleContainsResourceId(this.getMedicationRequestBundle(), id)
        || this.bundleContainsResourceId(this.getNutritionOrderBundle(), id)
        || this.bundleContainsResourceId(this.getServiceRequestBundle(), id)
        || this.bundleContainsResourceId(this.getSupplyRequestBundle(), id)
        || this.bundleContainsResourceId(this.getAppointmentBundle(), id)
        || this.bundleContainsResourceId(this.getEncounterBundle(), id)
        || this.bundleContainsResourceId(this.getMedicationDispenseBundle(), id)
        || this.bundleContainsResourceId(this.getMedicationStatementBundle(), id);
  }

  /**
   * Returns whether the given bundle contains the given resource.
   * @param bundle
   * @param id
   * @return
   */
  private boolean bundleContainsResourceId(IBaseResource bundle, String id) {
    if (bundle == null || bundle.getClass() != Bundle.class) {
      return false;
    }

    if (id.contains("/")) {
      String[] splitId = id.split("/");
      id = splitId[splitId.length-1];
    }
    final String idToCheck = id;
    return ((Bundle) bundle).getEntry().stream().anyMatch(entry -> entry.getResource().getId().contains(idToCheck));
  }

  @Override
  public String toString() {

    List<BundleEntryComponent> entries = new ArrayList<>();

    if(this.deviceRequestBundle != null && this.deviceRequestBundle.getClass() == Bundle.class) {
      entries.addAll(((Bundle)this.deviceRequestBundle).getEntry());
    }

    if(this.nutritionOrderBundle != null && this.nutritionOrderBundle.getClass() == Bundle.class){
      entries.addAll(((Bundle)this.nutritionOrderBundle).getEntry());
    }

    if(this.serviceRequestBundle != null && this.serviceRequestBundle.getClass() == Bundle.class){
      entries.addAll(((Bundle)this.serviceRequestBundle).getEntry());
    }

    if(this.medicationDispenseBundle != null && this.medicationDispenseBundle.getClass() == Bundle.class){
      entries.addAll(((Bundle)this.medicationDispenseBundle).getEntry());
    }

    if(this.medicationStatementBundle != null && this.medicationStatementBundle.getClass() == Bundle.class){
      entries.addAll(((Bundle)this.medicationStatementBundle).getEntry());
    }

    if(this.encounterBundle != null && this.encounterBundle.getClass() == Bundle.class){
      entries.addAll(((Bundle)this.encounterBundle).getEntry());
    }

    if(this.appointmentBundle != null && this.appointmentBundle.getClass() == Bundle.class){
      entries.addAll(((Bundle)this.appointmentBundle).getEntry());
    }

    if(this.medicationRequestBundle != null && this.medicationRequestBundle.getClass() == Bundle.class){
      entries.addAll(((Bundle)this.medicationRequestBundle).getEntry());
    }

    if(this.supplyRequestBundle != null && this.supplyRequestBundle.getClass() == Bundle.class){
      entries.addAll(((Bundle)this.supplyRequestBundle).getEntry());
    }

    if(this.coverageBundle != null && this.coverageBundle.getClass() == Bundle.class) {
      entries.addAll(((Bundle)this.coverageBundle).getEntry());
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
