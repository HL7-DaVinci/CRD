library Demo version '1.0.0'
using FHIR version '3.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers

parameter DeviceRequest DeviceRequest

define DevReqCode: DeviceRequest.code
define PatientMaybe: DeviceRequest
