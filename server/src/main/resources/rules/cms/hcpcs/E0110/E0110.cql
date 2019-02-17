library Rule version '0.1.0'
using FHIR version '3.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers

parameter Patient Patient
parameter device_request DeviceRequest
parameter practitioner_location Location

define "Age":
  AgeInYears()

define RULE_APPLIES:
  "Age" >= 21 and "Age" <= 50

define RESULT_Summary:
  'Auth required.'

define RESULT_Details:
  'Authorization is required, follow the attached link for information.'

define RESULT_InfoLink:
    'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNMattersArticles/downloads/MM3791.pdf'
