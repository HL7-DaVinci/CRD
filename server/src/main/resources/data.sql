INSERT
INTO
	COVERAGE_REQUIREMENT_RULES
(
	ID
,	AGE_RANGE_LOW
,	AGE_RANGE_HIGH
,	GENDER_CODE
,	PATIENT_ADDRESS_STATE
,	PROVIDER_ADDRESS_STATE
,	EQUIPMENT_CODE
,	CODE_SYSTEM
,	CQL
) VALUES
--	(0, 18, 80, NULL, NULL, NULL, '94660', 'http://www.ama-assn.org/go/cpt', 'library Demo version \'1.0.0\'\nusing FHIR version \'3.0.0\'\ninclude FHIRHelpers version \'3.0.0\' called FHIRHelpers\nparameter Patient Patient\nparameter DeviceRequest DeviceRequest\ndefine AuthNeeded: if (AgeInYears() > 10) then 0 else 1\ndefine InfoLink: \'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf\'')
	(0, 18, 80, NULL, NULL, NULL, 'E0424', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', 'library Demo version ''1.0.0'' using FHIR version ''3.0.0'' include FHIRHelpers version ''3.0.0'' called FHIRHelpers parameter Patient Patient parameter DeviceRequest DeviceRequest define RESULT_Summary: if (AgeInYears() > 10) then ''No auth needed'' else ''Auth is needed...'' define RESULT_Detail: ''Some details...'' define RESULT_InfoLink: ''https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf''')
--	(0, 18, 80, NULL, NULL, NULL, '94660', 'http://www.ama-assn.org/go/cpt', FALSE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf')
--,	(1, 18, 90, NULL, NULL, NULL, '97542', 'http://www.ama-assn.org/go/cpt', TRUE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PMDFactSheet07_Quark19.pdf')
--,	(2, 21, 50, NULL, NULL, NULL, 'E0110', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', FALSE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNMattersArticles/downloads/MM3791.pdf')
--,	(3, 8, 90, 'M', NULL, NULL, 'E0250', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', TRUE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforHospitalBedsandAccessories-ICN909476.pdf')
--,	(4, 10, 55, NULL, NULL, NULL, '95250', 'http://www.ama-assn.org/go/cpt', FALSE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforGlucoseMonitors-ICN909465.pdf')
--,	(5, 0, 90, NULL, NULL, NULL, '94640', 'http://www.ama-assn.org/go/cpt', FALSE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforNebulizersandRelatedDrugs-ICN909469.pdf')
--,	(6, 18, 60, NULL, NULL, NULL, '82947', 'http://www.ama-assn.org/go/cpt', FALSE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforDiabeticTestStrips-ICN909185.pdf')
--,	(7, 30, 80, NULL, NULL, NULL, 'A5500', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', TRUE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforDiabeticShoes-ICN909471.pdf')
--,	(8, 60, 90, 'M', NULL, NULL, 'E0130', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', FALSE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforWalkers-ICN909483.pdf')
--,	(9, 30, 80, NULL, NULL, NULL, '860195', 'http://www.nlm.nih.gov/research/umls/rxnorm', FALSE, 'https://www.medicare.gov/forms-help-and-resources/forms/medicare-forms.html')
--,	(10, 10, 70, NULL, NULL, NULL, '209431', 'http://www.nlm.nih.gov/research/umls/rxnorm', TRUE, 'https://en.wikipedia.org/wiki/Paracetamol')
--,	(11, 0, 100, NULL, NULL, NULL, 'E0250', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', FALSE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforHospitalBedsandAccessories-ICN909476.pdf')
--,	(12, 0, 100, NULL, 'MA', 'MA', 'E0424', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', FALSE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/a')
;
