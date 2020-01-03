library BloodGlucose version '0.0.1'
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0' called FHIRHelpers

//codesystem "ICD-10-CM": 'http://hl7.org/fhir/sid/icd-10-cm'
//codesystem "LOINC": 'http://loinc.org'
//codesystem "SNOMED-CT": 'http://snomed.info/sct'

parameter device_request DeviceRequest

context Patient

define Today: Today()




