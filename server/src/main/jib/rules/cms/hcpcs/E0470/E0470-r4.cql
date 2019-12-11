library Rule version '0.1.0'
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0' called FHIRHelpers

parameter Patient Patient
parameter device_request DeviceRequest

define "Age":
AgeInYears()

define RULE_APPLIES:
"Age" > 0

define RESULT_Summary:
  'Authorization not required.'

define RESULT_Details:
  'Authorization is not required.'

define RESULT_InfoLink:
    'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf'

define RESULT_QuestionnaireUri:
    'urn:hl7:davinci:crd:positive-airway-pressure-questionnaire'

define RESULT_requestId:
  device_request
