library Rule version '0.1.0'
using FHIR version '3.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers

parameter Patient Patient
parameter device_request DeviceRequest
parameter practitioner_location Location

define "Age":
  AgeInYears()

define "Gender":
  Patient.gender.value

define RULE_APPLIES:
  true

define "Auth_Needed":
  "Gender" = 'male'

define RESULT_Summary:
  if "Auth_Needed" then
    'Authorization required.'
  else
    'No authorization is required'

define RESULT_Details:
  if "Auth_Needed" then
    'Authorization is required, follow the attached link for information.'
  else
    'No authorization is required, as laid out at the attached link.'

define RESULT_InfoLink:
    'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforHospitalBedsandAccessories-ICN909476.pdf'
