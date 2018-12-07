library RULE version '0'

// measure in reference to a single patient not the entire world
context Patient

// Rule Definitions
// id = 4
define "Age Range Low": 10
define "Age Range High": 55
define "Gender Code": ''
define "Patient Address State": ''
define "Provider Address State": ''
define "Equipment Code": '95250'
define "Code System": 'http://www.ama-assn.org/go/cpt'
define "No Auth Needed": false
define "Info Link": 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforGlucoseMonitors-ICN909465.pdf'

define function "In Age Range"(age Integer):
    age >= "Age Range Low" and age <= "Age Range High"

define function "Correct Gender"(gender String):
    "Gender Code" = '' or gender = "Gender Code"

define function "Correct Patient State"(patientState String):
    "Patient Address State" = '' or patientState = "Patient Address State"
define function "Correct Provider State"(providerState String):
    "Provider Address State" = '' or providerState = "Provider Address State"
define function "Correct State"(patientState String, providerState String):
    "Correct Patient State"(patientState) and "Correct Provider State"(providerState)

define function "Correct Equipment Code"(equipmentCode String):
    equipmentCode = "Equipment Code"
define function "Correct Code System"(codeSystem String):
    codeSystem = "Code System"
define function "Correct Device"(equipmentCode String, codeSystem String):
    "Correct Equipment Code"(equipmentCode) and "Correct Code System"(codeSystem)


define function Applies(age Integer, gender String, patientState String, providerState String, equipmentCode String, codeSystem String):
    "In Age Range"(age) and "Correct Gender"(gender) and "Correct State"(patientState, providerState) and "Correct Device"(equipmentCode, codeSystem)

define "Documentation Required": not "No Auth Needed"

