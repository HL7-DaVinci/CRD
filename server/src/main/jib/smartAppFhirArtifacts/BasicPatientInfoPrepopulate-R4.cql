library BasicPatientInfo version '0.2.0'
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0' called FHIRHelpers

parameter device_request DeviceRequest
parameter service_request ServiceRequest

context Patient

define function GetMiddleInitials(name FHIR.HumanName):
  Substring(Combine((name.given given return Substring(given.value,0,1)),', '),3)

define Today: Today()

// Grab patient FHIR HumanName
define Name: singleton from (Patient.name name where name.use.value = 'official')

// Extract useful name elements
define LastName: "Name".family.value
define MiddleInitial: GetMiddleInitials("Name")
define FirstName: "Name".given[0].value

define Gender: Patient.gender.value
define DateOfBirth: Patient.birthDate.value

// pulling from insurance from both device request and service request
define CoverageResourceId:
  Coalesce(First(service_request.insurance), First(device_request.insurance)).reference.value

define CoverageResource: singleton from (
  [Coverage] coverage
    // pull coverage resource id from the device request insurance extension
    where ('Coverage/' + coverage.id) = "CoverageResourceId")

define MedicareId: "CoverageResource".subscriberId.value
