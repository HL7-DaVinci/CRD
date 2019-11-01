library Rule version '0.1.0'
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0' called FHIRHelpers

parameter Patient Patient
parameter medication_request MedicationRequest

define "Age":
  AgeInYears()

define RULE_APPLIES:
  "Age" >= 30 and "Age" <= 80

define RESULT_Summary:
  'Authorization is required.'

define RESULT_Details:
  'Authorization is required, follow the link for more information.'

define RESULT_InfoLink:
    'https://www.medicare.gov/forms-help-and-resources/forms/medicare-forms.html'

define RESULT_QuestionnaireUri:
    'urn:hl7:davinci:crd:botox-questionnaire'

define RESULT_requestId:
    medication_request
