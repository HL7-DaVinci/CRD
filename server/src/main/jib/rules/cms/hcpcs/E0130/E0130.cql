library Rule version '0.1.0'
using FHIR version '3.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers

parameter Patient Patient
parameter device_request DeviceRequest

define "Age":
  AgeInYears()

define "Gender":
  Patient.gender.value

define RULE_APPLIES:
  "Age" >= 60 and "Age" <= 90 and "Gender" = 'male'

define PRIORAUTH_REQUIRED:
  false

define DOCUMENTATION_REQUIRED:
  true

define RESULT_Summary:
  'Auth required.'

define RESULT_Details:
  'Authorization is required, follow the attached link for information.'

define RESULT_InfoLink:
    'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforWalkers-ICN909483.pdf'
