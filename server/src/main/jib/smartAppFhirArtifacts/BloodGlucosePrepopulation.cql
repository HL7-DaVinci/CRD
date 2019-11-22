library BloodGlucose version '0.0.1'
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0' called FHIRHelpers

//codesystem "ICD-10-CM": 'http://hl7.org/fhir/sid/icd-10-cm'
//codesystem "LOINC": 'http://loinc.org'
//codesystem "SNOMED-CT": 'http://snomed.info/sct'

parameter device_request DeviceRequest

context Patient

define function GetMiddleInitials(name FHIR.HumanName):
  Substring(Combine((name.given given return Substring(given.value,0,1)),', '),3)

define Today: Today()

// ordering provider info
define OrderingProvider: singleton from (
  [Practitioner] practitioner
    where ('Practitioner/' + practitioner.id) = device_request.performer.reference.value)
define OrderingProviderName: singleton from ("OrderingProvider".name name where name.use.value = 'official')
define OrderingProviderLastName: "OrderingProviderName".family.value
define OrderingProviderMiddleInitial: GetMiddleInitials("OrderingProviderName")
define OrderingProviderFirstName: "OrderingProviderName".given[0].value
define OrderingProviderNPI: (singleton from (
  "OrderingProvider".identifier identifier
    where identifier.system.value = 'http://hl7.org/fhir/sid/us-npi')).value.value


