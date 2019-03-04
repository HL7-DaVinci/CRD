library Rule version '0.1.0'
using FHIR version '3.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers

parameter Patient Patient
parameter device_request DeviceRequest
parameter practitioner_location Location

define "Age":
  AgeInYears()

define RULE_APPLIES:
  "Age" >= 30 and "Age" <= 80

define RESULT_Summary:
  'No auth needed'

define RESULT_Details:
  'No authorization is required, as laid out at the attached link.'

define RESULT_InfoLink:
    'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforDiabeticShoes-ICN909471.pdf'
