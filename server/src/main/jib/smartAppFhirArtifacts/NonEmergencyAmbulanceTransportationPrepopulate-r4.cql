library NonEmergencyAmbulanceTransportation  version '0.1.0'
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ICD-10-CM": 'http://hl7.org/fhir/sid/icd-10-cm'
codesystem "LOINC": 'http://loinc.org'
codesystem "SNOMED-CT": 'http://snomed.info/sct'

parameter service_request ServiceRequest

context Patient

define "ServiceRequestHcpcsCoding": singleton from (
  service_request.code.coding coding
    where coding[0].system.value = 'https://bluebutton.cms.gov/resources/codesystem/hcpcs')

define "ServiceRequestDescription": 'HCPCS ' + "ServiceRequestHcpcsCoding".code.value + ' - ' + "ServiceRequestHcpcsCoding".display.value
define "NeatServiceRequested":
  if "ServiceRequestHcpcsCoding".code.value = 'A0426' then 'A0426'
  else if  "ServiceRequestHcpcsCoding".code.value = 'A0428' then 'A0428'
  else 'null'

define "ServiceRequestReason": service_request.reasonCode[0].text.value

