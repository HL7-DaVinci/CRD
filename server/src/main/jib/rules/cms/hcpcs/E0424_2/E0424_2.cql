library Rule version '0.1.0'
using FHIR version '3.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers

parameter Patient Patient
parameter device_request DeviceRequest
parameter practitioner_location Location


define "Blah":
  FHIRHelpers.PlusOne(1)

define "Age":
  AgeInYears()

define "Gender":
  Patient.gender.value

define "PatientHomeState":
  Patient.address.where(use.value = 'home')[0].state.value

define "ProviderState":
  practitioner_location.address[0].state.value


define RULE_APPLIES:
  "Gender" = 'male'
  and "PatientHomeState" = 'MA'
  and "ProviderState" = 'MA'
  and "Blah" = 2

define "Auth_Needed":
  "Age" > 57

define RESULT_Summary:
  if "Auth_Needed" then
    'No auth needed'
  else
    'Auth required'

define RESULT_Details:
  if "Auth_Needed" then
    'No authorization is required, as laid out at the attached link.'
  else
    'Authorization is required, follow the attached link for information.'

define RESULT_InfoLink:
    'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/a'

define RESULT_QuestionnaireUri:
    'urn:hl7:davinci:crd:home-oxygen-questionnaire'

define RESULT_requestId:
  device_request
