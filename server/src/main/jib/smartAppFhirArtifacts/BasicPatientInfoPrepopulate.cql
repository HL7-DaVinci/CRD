library BasicPatientInfo version '0.0.1'
using FHIR version '3.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers

parameter device_request DeviceRequest

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

define CoverageResource: singleton from (
  [Coverage] coverage
    // pull coverage resource id from the device request insurance extension
    where ('Coverage/' + coverage.id) = ((device_request.extension ext where ext.url = 'http://build.fhir.org/ig/HL7/davinci-crd/STU3/ext-insurance.html')[0].value.reference.value))

define MedicareId: "CoverageResource".subscriberId.value
