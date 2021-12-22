package org.cdshooks;

import java.util.UUID;

public class CoverageRequirements {
  private boolean applies;
  private String summary;
  private String details;
  private String infoLink;
  private String questionnaireOrderUri;
  private String questionnaireFaceToFaceUri;
  private String questionnaireLabUri;
  private String questionnaireProgressNoteUri;
  private String questionnairePARequestUri;
  private String questionnairePlanOfCareUri;
  private String questionnaireDispenseUri;
  private String questionnaireAdditionalUri;
  private String requestId;
  private boolean priorAuthRequired;
  private boolean documentationRequired;

  private boolean priorAuthApproved;
  private String priorAuthId;

  public boolean getApplies() { return applies; }

  public CoverageRequirements setApplies(boolean applies) {
    this.applies = applies;
    return this;
  }

  public String getSummary() {
    return summary;
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
  public String getQuestionnaireOrderUri() {
    return questionnaireOrderUri;
  }

  public CoverageRequirements setQuestionnaireOrderUri(String questionnaireOrderUri) {
    this.questionnaireOrderUri = questionnaireOrderUri;
    return this;
  }

  public String getQuestionnaireFaceToFaceUri() {
    return this.questionnaireFaceToFaceUri;
  }

  public CoverageRequirements setQuestionnaireFaceToFaceUri(String questionnaireFaceToFaceUri) {
    this.questionnaireFaceToFaceUri = questionnaireFaceToFaceUri;
    return this;
  }

  public String getQuestionnaireLabUri() {
    return questionnaireLabUri;
  }

  public CoverageRequirements setQuestionnaireLabUri(String questionnaireLabUri) {
    this.questionnaireLabUri = questionnaireLabUri;
    return this;
  }

  public String getQuestionnaireProgressNoteUri() {
    return questionnaireProgressNoteUri;
  }

  public CoverageRequirements setQuestionnaireProgressNoteUri(String questionnaireProgressNoteUri) {
    this.questionnaireProgressNoteUri = questionnaireProgressNoteUri;
    return this;
  }

  public String getQuestionnairePARequestUri() {
    return questionnairePARequestUri;
  }

  public CoverageRequirements setQuestionnairePARequestUri(String questionnairePARequestUri) {
    this.questionnairePARequestUri = questionnairePARequestUri;
    return this;
  }

  public String getQuestionnairePlanOfCareUri() {
    return questionnairePlanOfCareUri;
  }

  public CoverageRequirements setQuestionnairePlanOfCareUri(String questionnairePlanOfCareUri) {
    this.questionnairePlanOfCareUri = questionnairePlanOfCareUri;
    return this;
  }

  public String getQuestionnaireDispenseUri() {
    return questionnaireDispenseUri;
  }

  public CoverageRequirements setQuestionnaireDispenseUri(String questionnaireDispenseUri) {
    this.questionnaireDispenseUri = questionnaireDispenseUri;
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

  public String getQuestionnaireAdditionalUri() {
    return questionnaireAdditionalUri;
  }

  public void setQuestionnaireAdditionalUri(String questionnaireAdditionalUri) {
    this.questionnaireAdditionalUri = questionnaireAdditionalUri;
  }
}
