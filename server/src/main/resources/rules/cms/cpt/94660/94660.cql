library Rule version '0.1.0'
using FHIR version '3.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers

parameter Patient Patient
parameter device_request DeviceRequest
parameter practitioner_location Location

define "Age":
  AgeInYears()

define RULE_APPLIES:
  "Age" >= 18 and "Age" <= 80

define RESULT_Summary:
  'Authorization required.'

define RESULT_Details:
  'Authorization is required, follow the link for more information.'

define RESULT_InfoLink:
    'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf'

