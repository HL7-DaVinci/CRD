package org.hl7.davinci.endpoint.vsac;

import java.util.HashMap;
import java.util.Map;

/**
 * Translates VSAC CodeSystem OIDs to FHIR CodeSystem URLs.
 */
public class CodeSystemTranslator {
  public static Map<String, String> oidToUriMap = new HashMap<String, String>();

  /**
   * Mapping of oids to uris taken from https://cts.nlm.nih.gov/fhir/ on 4/2/2020.
   * 
   * Converted with regex find/replace in VS Code:
   *     find - ([A-za-z0-9\-]+)\t([/:.A-za-z0-9\-]+)\t([0-9.]+)
   *  replace - oidToUriMap.put("$3", "$2"); //$1
   */
  static {
    oidToUriMap.put("2.16.840.1.113883.5.4", "http://hl7.org/fhir/v3/ActCode"); //ActCode
    oidToUriMap.put("2.16.840.1.113883.5.1001", "http://hl7.org/fhir/v3/ActMood"); //ActMood
    oidToUriMap.put("2.16.840.1.113883.5.7", "http://hl7.org/fhir/v3/ActPriority"); //ActPriority
    oidToUriMap.put("2.16.840.1.113883.5.8", "http://hl7.org/fhir/v3/ActReason"); //ActReason
    oidToUriMap.put("2.16.840.1.113883.5.1002", "http://hl7.org/fhir/v3/ActRelationshipType"); //ActRelationshipType
    oidToUriMap.put("2.16.840.1.113883.5.14", "http://hl7.org/fhir/v3/ActStatus"); //ActStatus
    oidToUriMap.put("2.16.840.1.113883.5.1119", "http://hl7.org/fhir/v3/AddressUse"); //AddressUse
    oidToUriMap.put("2.16.840.1.113883.5.1", "http://hl7.org/fhir/v3/AdministrativeGender"); //AdministrativeGender
    oidToUriMap.put("2.16.840.1.113883.18.2", "http://hl7.org/fhir/v2/0001"); //AdministrativeSex
    oidToUriMap.put("2.16.840.1.113883.6.13", "http://www.nlm.nih.gov/research/umls/cdt"); //CDT
    oidToUriMap.put("2.16.840.1.113883.6.12", "http://www.ama-assn.org/go/cpt"); //CPT
    oidToUriMap.put("2.16.840.1.113883.12.292", "http://hl7.org/fhir/sid/cvx"); //CVX
    oidToUriMap.put("2.16.840.1.113883.5.25", "http://hl7.org/fhir/v3/Confidentiality"); //Confidentiality
    oidToUriMap.put("2.16.840.1.113883.12.112", "http://hl7.org/fhir/v2/0112"); //DischargeDisposition
    oidToUriMap.put("2.16.840.1.113883.5.43", "http://hl7.org/fhir/v3/EntityNamePartQualifier"); //EntityNamePartQualifier
    oidToUriMap.put("2.16.840.1.113883.5.45", "http://hl7.org/fhir/v3/EntityNameUse"); //EntityNameUse
    oidToUriMap.put("2.16.840.1.113883.6.90", "http://hl7.org/fhir/sid/icd-10-cm"); //ICD10CM
    oidToUriMap.put("2.16.840.1.113883.6.4", "http://www.icd10data.com/icd10pcs"); //ICD10PCS
    oidToUriMap.put("2.16.840.1.113883.6.103", "http://hl7.org/fhir/sid/icd-9-cm"); //ICD9CM
    oidToUriMap.put("2.16.840.1.113883.6.104", "http://hl7.org/fhir/sid/icd-9-cm"); //ICD9CM
    oidToUriMap.put("2.16.840.1.113883.6.1", "http://loinc.org"); //LOINC
    oidToUriMap.put("2.16.840.1.113883.5.60", "http://hl7.org/fhir/v3/LanguageAbilityMode"); //LanguageAbilityMode
    oidToUriMap.put("2.16.840.1.113883.5.61", "http://hl7.org/fhir/v3/LanguageAbilityProficiency"); //LanguageAbilityProficiency
    oidToUriMap.put("2.16.840.1.113883.5.63", "http://hl7.org/fhir/v3/LivingArrangement"); //LivingArrangement
    oidToUriMap.put("2.16.840.1.113883.5.2", "http://hl7.org/fhir/v3/MaritalStatus"); //MaritalStatus
    oidToUriMap.put("2.16.840.1.113883.6.345", "http://www.nlm.nih.gov/research/umls/MED-RT"); //MED-RT
    oidToUriMap.put("2.16.840.1.113883.3.26.1.1", "http://ncimeta.nci.nih.gov"); //NCI
    oidToUriMap.put("2.16.840.1.113883.3.26.1.5", "http://hl7.org/fhir/ndfrt"); //NDFRT
    oidToUriMap.put("2.16.840.1.113883.6.101", "http://nucc.org/provider-taxonomy"); //NUCCPT
    oidToUriMap.put("2.16.840.1.113883.5.1008", "http://hl7.org/fhir/v3/NullFlavor"); //NullFlavor
    oidToUriMap.put("2.16.840.1.113883.5.83", "http://hl7.org/fhir/v3/ObservationInterpretation"); //ObservationInterpretation
    oidToUriMap.put("2.16.840.1.113883.5.1063", "http://hl7.org/fhir/v3/ObservationValue"); //ObservationValue
    oidToUriMap.put("2.16.840.1.113883.5.88", "http://hl7.org/fhir/v3/ParticipationFunction"); //ParticipationFunction
    oidToUriMap.put("2.16.840.1.113883.5.1064", "http://hl7.org/fhir/v3/ParticipationMode"); //ParticipationMode
    oidToUriMap.put("2.16.840.1.113883.5.90", "http://hl7.org/fhir/v3/ParticipationType"); //ParticipationType
    oidToUriMap.put("2.16.840.1.113883.6.88", "http://www.nlm.nih.gov/research/umls/rxnorm"); //RXNORM
    oidToUriMap.put("2.16.840.1.113883.5.1076", "http://hl7.org/fhir/v3/ReligiousAffiliation"); //ReligiousAffiliation
    oidToUriMap.put("2.16.840.1.113883.5.110", "http://hl7.org/fhir/v3/RoleClass"); //RoleClass
    oidToUriMap.put("2.16.840.1.113883.5.111", "http://hl7.org/fhir/v3/RoleCode"); //RoleCode
    oidToUriMap.put("2.16.840.1.113883.5.1068", "http://hl7.org/fhir/v3/RoleStatus"); //RoleStatus
    oidToUriMap.put("2.16.840.1.113883.6.96", "http://snomed.info/sct"); //SNOMEDCT
    oidToUriMap.put("2.16.840.1.113883.3.221.5", "http://www.nlm.nih.gov/research/umls/sop"); //SOP
    oidToUriMap.put("1.3.6.1.4.1.12009.10.3.1", "http://unitsofmeasure.org"); //UCUM
    oidToUriMap.put("2.16.840.1.113883.6.86", "http://www.nlm.nih.gov/research/umls"); //UMLS
    oidToUriMap.put("2.16.840.1.113883.4.9", "http://fdasis.nlm.nih.gov"); //UNII
    oidToUriMap.put("2.16.840.1.113883.5.79", "http://hl7.org/fhir/v3/MediaType"); //mediaType

    // Extra oid mappings not yet defined by VSAC FHIR API
    // HCPCS is still in decision on HL7 JIRA https://jira.hl7.org/browse/FHIR-13129?jql=project%20%3D%20fhir%20and%20text%20~%20hcpcs
    oidToUriMap.put("2.16.840.1.113883.6.285", "https://bluebutton.cms.gov/resources/codesystem/hcpcs"); //HCPCS
  }

  public static String convertOidToUri(String oid) {
    String uri = oidToUriMap.get(oid);
    if (uri == null) {
      return oid;
    } else {
      return uri;
    }
  }
}