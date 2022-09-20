package org.cdshooks;

import java.util.ArrayList;
import java.util.UUID;

public class CoverageRequirements {
  private boolean applies;
  private String summary;
  private String details;
  private String infoLink;
  private ArrayList<Requirement> patientRequirements;
  private ArrayList<Requirement> prescriberRequirements;
  private String requestId;
  private boolean priorAuthRequired;
  private boolean documentationRequired;

  private boolean priorAuthApproved;
  private String priorAuthId;

  public CoverageRequirements() {
    this.patientRequirements = new ArrayList<>();
    this.prescriberRequirements = new ArrayList<>();
  }

  public boolean getApplies() { return applies; }

  public CoverageRequirements setApplies(boolean applies) {
    this.applies = applies;
    return this;
  }

  public String getSummary() {
    return summary;
  }

  public ArrayList<Requirement> getPatientRequirements() {
    return patientRequirements;
  }

  public void addPatientRequirement(Requirement patientRequirement) {
    if(patientRequirement.getUrl() != null) {
      this.patientRequirements.add(patientRequirement);
    }
  }


  public ArrayList<Requirement> getPrescriberRequirements() {
    return prescriberRequirements;
  }

  public void addPrescriberRequirement(Requirement prescriberRequirement) {
    if(prescriberRequirement.getUrl() != null) {
      this.prescriberRequirements.add(prescriberRequirement);
    }
  }

  public CoverageRequirements setSummary(String summary) {
    this.summary = summary;
    return this;
  }

  public String getDetails() {
    return details;
  }

  public CoverageRequirements setDetails(String details) {
    this.details = details;
    return this;
  }

  public String getInfoLink() {
    return infoLink;
  }

  public CoverageRequirements setInfoLink(String infoLink) {
    this.infoLink = infoLink;
    return this;
  }

  public String getRequestId() {
    return requestId;
  }

  public CoverageRequirements setRequestId(String requestId) {
    this.requestId = requestId;
    return this;
  }

  public boolean isPriorAuthRequired() {
    return priorAuthRequired;
  }

  public CoverageRequirements setPriorAuthRequired(boolean priorAuthRequired) {
    this.priorAuthRequired = priorAuthRequired;
    return this;
  }

  public boolean isDocumentationRequired() {
    return documentationRequired;
  }

  public CoverageRequirements setDocumentationRequired(boolean documentationRequired) {
    this.documentationRequired = documentationRequired;
    return this;
  }

  public boolean isPriorAuthApproved() { return priorAuthApproved; }

  public CoverageRequirements setPriorAuthApproved(boolean priorAuthApproved) {
    this.priorAuthApproved = priorAuthApproved;
    return this;
  }

  public String getPriorAuthId() { return priorAuthId; }

  public CoverageRequirements setPriorAuthId(String priorAuthId) {
    this.priorAuthId = priorAuthId;
    return this;
  }

  public CoverageRequirements generatePriorAuthId() {
    // Generate a random UUID as the ID. This is the same method that Prior Auth (PAS) uses.
    this.priorAuthId = UUID.randomUUID().toString();
    return this;
  }

}
