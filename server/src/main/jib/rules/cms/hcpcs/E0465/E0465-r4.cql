library Rule version '0.1.0'
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0' called FHIRHelpers

parameter Patient Patient
parameter device_request DeviceRequest

define RULE_APPLIES:
  true

define RESULT_Summary:
  'Documentation Required.'

define RESULT_Details:
  'Documentation Required, please complete form via Smart App link.'

define RESULT_InfoLink:
    'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforVentilators-MLN9725344Print-Friendly.pdf'

define RESULT_QuestionnaireOrderUri:
    'urn:hl7:davinci:crd:ventilator-questionnaire-r4'

define RESULT_requestId:
  device_request
