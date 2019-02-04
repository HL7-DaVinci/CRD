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
,	AUTH_REQUIRED
,	INFO_LINK
, PRICE_DESCRIPTION
) VALUES
	(0, 18, 80, NULL, NULL, NULL, '94660', 'http://www.ama-assn.org/go/cpt', TRUE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PAP_DocCvg_Factsheet_ICN905064.pdf', 'Payor will cover up to $2050.')
,	(1, 18, 90, NULL, NULL, NULL, '97542', 'http://www.ama-assn.org/go/cpt', FALSE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/downloads/PMDFactSheet07_Quark19.pdf', 'Payor will cover up to $980.')
,	(2, 21, 50, NULL, NULL, NULL, 'E0110', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', TRUE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNMattersArticles/downloads/MM3791.pdf', 'Payor will cover 80% up to $3200.')
,	(3, 8, 90, 'M', NULL, NULL, 'E0250', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', FALSE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforHospitalBedsandAccessories-ICN909476.pdf', 'Payor will cover up to $1200.')
,	(4, 10, 55, NULL, NULL, NULL, '95250', 'http://www.ama-assn.org/go/cpt', TRUE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforGlucoseMonitors-ICN909465.pdf', 'Payor will cover 75% up to $4000.')
,	(5, 0, 90, NULL, NULL, NULL, '94640', 'http://www.ama-assn.org/go/cpt', TRUE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforNebulizersandRelatedDrugs-ICN909469.pdf', 'Payor will cover up to $350.')
,	(6, 18, 60, NULL, NULL, NULL, '82947', 'http://www.ama-assn.org/go/cpt', TRUE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforDiabeticTestStrips-ICN909185.pdf', 'Payor will cover up to $9800.')
,	(7, 30, 80, NULL, NULL, NULL, 'A5500', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', FALSE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforDiabeticShoes-ICN909471.pdf', 'Payor will cover 50% up to $7500.')
,	(8, 60, 90, 'M', NULL, NULL, 'E0130', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', TRUE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforWalkers-ICN909483.pdf', 'Payor will cover up to $4300.')
,	(9, 30, 80, NULL, NULL, NULL, '860195', 'http://www.nlm.nih.gov/research/umls/rxnorm', TRUE, 'https://www.medicare.gov/forms-help-and-resources/forms/medicare-forms.html', 'Payor will cover up to $680.')
,	(10, 10, 70, NULL, NULL, NULL, '209431', 'http://www.nlm.nih.gov/research/umls/rxnorm', FALSE, 'https://en.wikipedia.org/wiki/Paracetamol', 'Payor will cover up to $1050.')
,	(11, 10, 70, NULL, NULL, NULL, '307675', 'http://www.nlm.nih.gov/research/umls/rxnorm', FALSE, 'https://www.rxlist.com/consumer_acetaminophen_tylenol/drugs-condition.htm', 'Payor will cover 80% up to $5000.')
,	(12, 0, 100, NULL, NULL, NULL, 'E0250', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', TRUE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/ProviderComplianceTipsforHospitalBedsandAccessories-ICN909476.pdf', 'Payor will cover up to $1300.')
,	(13, 0, 100, NULL, 'MA', 'MA', 'E0424', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', TRUE, 'https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/a',
'#### Patient Monthly Cost Information (Florida)
| Metro Copay | Rural Copay |
| :---------: |:-----------:|
| $14.64      | $26.94      |')
,	(14, 0, 100, NULL, NULL, NULL, 'E0433', 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', TRUE, '	https://www.cms.gov/Outreach-and-Education/Medicare-Learning-Network-MLN/MLNProducts/Downloads/Home-Oxygen-Therapy-Text-Only.pdf',
'#### Patient Monthly Cost Information (Florida)
| Metro Copay | Rural Copay |
| :---------: |:-----------:|
| $7.32       | $13.47      |')
;
