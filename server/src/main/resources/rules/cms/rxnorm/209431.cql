library Rule version '0.1.0'
using FHIR version '3.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers

parameter Patient Patient
parameter device_request DeviceRequest
parameter practitioner_location Location

define "Age":
  AgeInYears()

define RULE_APPLIES:
  "Age" >= 10 and "Age" <= 70

define RESULT_Summary:
  'No authorization required.'

define RESULT_Details:
  'No authorization is required, follow the link for more information.'

define RESULT_InfoLink:
    'https://en.wikipedia.org/wiki/Paracetamol'

