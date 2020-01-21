library NonEmergencyAmbulanceTransportation  version '0.1.0'
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0' called FHIRHelpers

codesystem "ICD-10-CM": 'http://hl7.org/fhir/sid/icd-10-cm'
codesystem "LOINC": 'http://loinc.org'
codesystem "SNOMED-CT": 'http://snomed.info/sct'

parameter service_request ServiceRequest

context Patient

// coverage requirement info
define OsaDiagnosis:
  if exists(Confirmed(ActiveOrRecurring([Condition: "OSA_Codes"]))) then 'OSA' else 'null'


define OtherDiagnoses:
  distinct(flatten(
    [Condition] C
      let ICD10Codings:
        ((C.code.coding) CODING where CODING.system.value in {
          'http://hl7.org/fhir/sid/icd-10',
          'http://hl7.org/fhir/sid/icd-10-cm'
        }
        return CODING.code.value + ' - ' + CODING.display.value)
      where C.clinicalStatus.coding.code = 'active'
      and exists(ICD10Codings)
      return ICD10Codings
  ))


define ServiceRequestHcpcsCoding: singleton from (
  service_request.code.coding
    where coding.system.value = 'https://bluebutton.cms.gov/resources/codesystem/hcpcs')

define ServiceRequestDescription: 'HCPCS ' + "ServiceRequestHcpcsCoding".code.value + ' - ' + "ServiceRequestHcpcsCoding".display.value
define NeatServiceRequested:
  if "ServiceRequestHcpcsCoding".code.value = 'A0426' then 'A0426'
  else if  "DeviceRequestHcpcsCoding".code.value = 'A0428' then 'A0428'
  else 'null'

////////////////////////////// Taken from CDS Connect Commons for FHIR, could replace with r4 version of helper library
define function ActiveOrRecurring(CondList List<Condition>):
  CondList C where C.clinicalStatus.coding.code in {'active', 'relapse'}

define function ObservationLookBack(ObsList List<Observation>, LookBack System.Quantity):
  ObsList O
    let LookBackInterval: Interval[Now() - LookBack, Now()]
    where (cast O.effective as dateTime).value in LookBackInterval
      or NullSafeToInterval(cast O.effective as Period) overlaps LookBackInterval
      or FHIRHelpers."ToDateTime"(O.issued) in LookBackInterval

define function NullSafeToInterval(Pd FHIR.Period):
  if Pd is not null then Interval[Pd."start".value, Pd."end".value] else null

define function Verified(ObsList List<Observation>):
  ObsList O where O.status.value in {'final', 'amended'}

define function WithUnit(ObsList List<Observation>, Unit String):
  ObsList O where (cast O.value as Quantity).unit.value = Unit or (cast O.value as Quantity).code.value = Unit

define function HighestObservation(ObsList List<Observation>):
  Max(ObsList O return NullSafeToQuantity(cast O.value as Quantity))

define function Confirmed(CondList List<Condition>):
  CondList C where C.verificationStatus.coding.code = 'confirmed'

define function NullSafeToQuantity(Qty FHIR.Quantity):
  if Qty is not null then
    System.Quantity {
      value: Qty.value.value,
      unit: Coalesce(Qty.unit.value, Qty.code.value)
    }
  else null

define function LowestObservation(ObsList List<Observation>):
  Min(ObsList O return NullSafeToQuantity(cast O.value as Quantity))
