library Rule version '0.1.0'
using FHIR version '3.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers

parameter Patient Patient
parameter device_request DeviceRequest

define "Age":
  AgeInYears()

define RULE_APPLIES:
  "Age" >= 18 and "Age" <= 90

define PRIORAUTH_REQUIRED:
  false

define DOCUMENTATION_REQUIRED:
  true

define RESULT_Summary:
  'No authorization required.'

define RESULT_Details:
  'No authorization is required, follow the link for more information.'

define RESULT_InfoLink:
    'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PMDFactSheet07_Quark19.pdf'
