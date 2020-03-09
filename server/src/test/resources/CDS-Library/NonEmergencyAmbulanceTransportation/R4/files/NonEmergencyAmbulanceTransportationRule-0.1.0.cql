library NonEmergencyAmbulanceTransportationRule version '0.1.0'
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0' called FHIRHelpers

parameter Patient Patient
parameter service_request ServiceRequest

define RULE_APPLIES:
  true

define PRIORAUTH_REQUIRED:
  true 

define DOCUMENTATION_REQUIRED:
  true

define RESULT_Summary:
  'Prior Authorization required.'

define RESULT_Details:
  'Prior Authorization required, follow the attached link for information.'

define RESULT_InfoLink:
  'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNMattersArticles/Downloads/SE1514.pdf'

// TODO - need to define the questionnaire in next story
define RESULT_QuestionnaireOrderUri:
  'urn:hl7:davinci:crd:non-emergency-ambulance-questionnaire-r4'

define RESULT_requestId:
  service_request
