library PositiveAirwayPressureDevice  version '0.0.1'
using FHIR version '3.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers


// This cql and questionnaire combo can be considered a partial implementation of these forms:
// https://www.cms.gov/Medicare/CMS-Forms/CMS-Forms/Downloads/CMS484.pdf
// https://www.cms.gov/Research-Statistics-Data-and-Systems/Computer-Data-and-Systems/Electronic-Clinical-Templates/Downloads/Home-Oxygen-Therapy-Order-Template-Draft-20170905-R40.pdf
// with guidance from https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/Home-Oxygen-Therapy-ICN908804.pdf


codesystem "ICD-10-CM": 'http://hl7.org/fhir/sid/icd-10-cm'
codesystem "LOINC": 'http://loinc.org'
codesystem "SNOMED-CT": 'http://snomed.info/sct'

//Obstructive Sleep Apnea
code "G47.33": 'G47.33' from "ICD-10-CM"

//Excessive Daytime Sleepiness
code "G47.1": 'G47.1' from "ICD-10-CM"
code "G47.10": 'G47.10' from "ICD-10-CM"
code "G47.11": 'G47.11' from "ICD-10-CM"
code "G47.12": 'G47.12' from "ICD-10-CM"
code "G47.13": 'G47.13' from "ICD-10-CM"
code "G47.14": 'G47.14' from "ICD-10-CM"
code "G47.19": 'G47.19' from "ICD-10-CM"

//Mood Disorder
code "F06.3": 'F06.3' from "ICD-10-CM"
code "F06.30": 'F06.30' from "ICD-10-CM"
code "F06.31": 'F06.31' from "ICD-10-CM"
code "F06.32": 'F06.32' from "ICD-10-CM"
code "F06.33": 'F06.33' from "ICD-10-CM"
code "F06.34": 'F06.34' from "ICD-10-CM"
code "F34.9": 'F34.9' from "ICD-10-CM"


//Impaired Cognition
code "G31.84": 'G31.84' from "ICD-10-CM"  // Mild cognitive impairment




parameter device_request DeviceRequest

// Relevant Diagnosis Code Lists
define "OSA_Codes": { "G47.33" }

define "Excessive_Daytime_Sleepiness": { "G47.1", "G47.10", "G47.11", "G47.12", "G47.13", "G47.14", "G47.19" }

// Observation code lists


// Other codes


context Patient


define function GetMiddleInitials(name FHIR.HumanName):
  Substring(Combine((name.given given return Substring(given.value,0,1)),', '),3)


define Today: Today()

// basic patient stuff
define PatientName: singleton from (Patient.name name where name.use.value = 'official')
define PatientLastName: "PatientName".family.value
define PatientMiddleInitial: GetMiddleInitials("PatientName")
define PatientFirstName: "PatientName".given[0].value
define PatientGender: Patient.gender.value
define PatientDateOfBirth: Patient.birthDate.value

// coverage requirement info
define OsaDiagnosis: {
  if exists(Confirmed(ActiveOrRecurring([Condition: "OSA_Codes"]))) then 'OSA' else 'null'
} except { 'null' } // except 'null' is a workaround since I don't see how to remove actual nulls

define "ICD10 Codings from Active Conditions":
  distinct(flatten(
    [Condition] C
      let ICD10Codings:
        (C.code.coding) CODING where CODING.system.value in {
          'http://hl7.org/fhir/sid/icd-10',
          'http://hl7.org/fhir/sid/icd-10-cm'
        }
      where C.clinicalStatus.value = 'active'
      and exists(ICD10Codings)
      return ICD10Codings
  ))
  
// Current Sleep study results
//Not clear how a sleep study will be represented in FHIR


// Medications
// Only using medication from the MedicationStatement FHIR resource.
// This will need to be expended to take into account MedicationRequest.
define MedicationList:
  [MedicationStatement] MS
  where MS.code.coding.system.value = 'http://www.nlm.nih.gov/research/umls/rxnorm'

// Allergies


// Treatment plan
// CarePlan
define PapRelatedCarePlan:
  [CarePlan] CP:
    where CP.status.code.value = 'active'

// device request info
define DeviceRequestHcpcsCoding: singleton from (
  ((cast device_request.code as CodeableConcept).coding) coding
    where coding.system.value = 'https://bluebutton.cms.gov/resources/codesystem/hcpcs')

define DeviceRequestDescription: 'HCPCS ' + "DeviceRequestHcpcsCoding".code.value + ' - ' + "DeviceRequestHcpcsCoding".display.value
define PapDeviceRequested: "DeviceRequestHcpcsCoding".code.value in { 'E0601', 'E0470' }


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






////////////////////////////// Taken from CDS Connect Commons for FHIR, could replace with stu3 version of helper library
define function ActiveOrRecurring(CondList List<Condition>):
  CondList C where C.clinicalStatus.value in {'active', 'relapse'}

define function ObservationLookBack(ObsList List<Observation>, LookBack System.Quantity):
  ObsList O
    let LookBackInterval: Interval[Now() - LookBack, Now()]
    where (cast O.effective as dateTime).value in LookBackInterval
      or NullSafeToInterval(cast O.effective as Period) overlaps LookBackInterval
      or O.issued in LookBackInterval

define function NullSafeToInterval(Pd FHIR.Period):
  if Pd is not null then Interval[Pd."start".value, Pd."end".value] else null

define function Verified(ObsList List<Observation>):
  ObsList O where O.status.value in {'final', 'amended'}

define function WithUnit(ObsList List<Observation>, Unit String):
  ObsList O where (cast O.value as Quantity).unit.value = Unit or (cast O.value as Quantity).code.value = Unit

define function HighestObservation(ObsList List<Observation>):
  Max(ObsList O return NullSafeToQuantity(cast O.value as Quantity))

define function Confirmed(CondList List<Condition>):
  CondList C where C.verificationStatus.value = 'confirmed'

define function NullSafeToQuantity(Qty FHIR.Quantity):
  if Qty is not null then
    System.Quantity {
      value: Qty.value.value,
      unit: Coalesce(Qty.unit.value, Qty.code.value)
    }
  else null

define function LowestObservation(ObsList List<Observation>):
  Min(ObsList O return NullSafeToQuantity(cast O.value as Quantity))

