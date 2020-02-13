library BloodGlucose version '0.0.1'
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0' called FHIRHelpers
include "CDS_Connect_Commons_for_FHIRv400" version '1.0.1' called C3F 

valueset "Diabetes Mellitus VS": 'urn:hl7:davinci:crd:valueset-diabetes'

//codesystem "ICD-10-CM": 'http://hl7.org/fhir/sid/icd-10-cm'
//codesystem "LOINC": 'http://loinc.org'
//codesystem "SNOMED-CT": 'http://snomed.info/sct'

parameter device_request DeviceRequest

context Patient

define Today: Today()

define "Diabetes Dignosis":
  [Condition: "Diabetes Mellitus VS"]

define "Has Active Diabetes Mellitus":
  exists(C3F.ActiveOrRecurring("Diabetes Dignosis"))




