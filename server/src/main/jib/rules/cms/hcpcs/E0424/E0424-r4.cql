library Rule version '0.1.0'
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0' called FHIRHelpers

parameter Patient Patient
parameter device_request DeviceRequest

define "Age":
  AgeInYears()

define RULE_APPLIES:
  "Age" > 0

define PRIORAUTH_REQUIRED:
  false

define DOCUMENTATION_REQUIRED:
  true

define RESULT_Summary:
  'Yes, this DME item has Coverage Requirements.'

define RESULT_Details:
  'Coverage Requirement details are specified, in the **Documentation Requirements** link below. Click the **SMART App** link below to begin the Coverage Requirements questionnaire.'

define RESULT_InfoLink:
    'https://www.cms.gov/outreach-and-education/medicare-learning-network-mln/mlnproducts/downloads/home-oxygen-therapy-text-only.pdf'

define RESULT_QuestionnaireUri:
    'urn:hl7:davinci:crd:home-oxygen-questionnaire_2-r4'

define RESULT_requestId:
    device_request
