const grant_type = "authorization_code";
const stateOptions = [
    { key: 'AL', value: 'AL', text: 'Alabama' },
    { key: 'AK', value: 'AK', text: 'Alaska' },
    { key: 'AZ', value: 'AZ', text: 'Arizona' },
    { key: 'AR', value: 'AR', text: 'Arkansas' },
    { key: 'CA', value: 'CA', text: 'California' },
    { key: 'CO', value: 'CO', text: 'Colorado' },
    { key: 'CT', value: 'CT', text: 'Connecticut' },
    { key: 'DE', value: 'DE', text: 'Delaware' },
    { key: 'DC', value: 'DC', text: 'District Of Columbia' },
    { key: 'FL', value: 'FL', text: 'Florida' },
    { key: 'GA', value: 'GA', text: 'Georgia' },
    { key: 'HI', value: 'HI', text: 'Hawaii' },
    { key: 'ID', value: 'ID', text: 'Idaho' },
    { key: 'IL', value: 'IL', text: 'Illinois' },
    { key: 'IN', value: 'IN', text: 'Indiana' },
    { key: 'IA', value: 'IA', text: 'Iowa' },
    { key: 'KS', value: 'KS', text: 'Kansas' },
    { key: 'KY', value: 'KY', text: 'Kentucky' },
    { key: 'LA', value: 'LA', text: 'Louisiana' },
    { key: 'ME', value: 'ME', text: 'Maine' },
    { key: 'MD', value: 'MD', text: 'Maryland' },
    { key: 'MA', value: 'MA', text: 'Massachusetts' },
    { key: 'MI', value: 'MI', text: 'Michigan' },
    { key: 'MN', value: 'MN', text: 'Minnesota' },
    { key: 'MS', value: 'MS', text: 'Mississippi' },
    { key: 'MO', value: 'MO', text: 'Missouri' },
    { key: 'MT', value: 'MT', text: 'Montana' },
    { key: 'NE', value: 'NE', text: 'Nebraska' },
    { key: 'NV', value: 'NV', text: 'Nevada' },
    { key: 'NH', value: 'NH', text: 'New Hampshire' },
    { key: 'NJ', value: 'NJ', text: 'New Jersey' },
    { key: 'NM', value: 'NM', text: 'New Mexico' },
    { key: 'NY', value: 'NY', text: 'New York' },
    { key: 'NC', value: 'NC', text: 'North Carolina' },
    { key: 'ND', value: 'ND', text: 'North Dakota' },
    { key: 'OH', value: 'OH', text: 'Ohio' },
    { key: 'OK', value: 'OK', text: 'Oklahoma' },
    { key: 'OR', value: 'OR', text: 'Oregon' },
    { key: 'PA', value: 'PA', text: 'Pennsylvania' },
    { key: 'RI', value: 'RI', text: 'Rhode Island' },
    { key: 'SC', value: 'SC', text: 'South Carolina' },
    { key: 'SD', value: 'SD', text: 'South Dakota' },
    { key: 'TN', value: 'TN', text: 'Tennessee' },
    { key: 'TX', value: 'TX', text: 'Texas' },
    { key: 'UT', value: 'UT', text: 'Utah' },
    { key: 'VT', value: 'VT', text: 'Vermont' },
    { key: 'VA', value: 'VA', text: 'Virginia' },
    { key: 'WA', value: 'WA', text: 'Washington' },
    { key: 'WV', value: 'WV', text: 'West Virginia' },
    { key: 'WI', value: 'WI', text: 'Wisconsin' },
    { key: 'WY', value: 'WY', text: 'Wyoming' },
  ]

const coverageCodes = [
    {key: 'group', value: 'group', text: 'Group'},
    {key: 'subgroup', value: 'subgroup', text: 'SubGroup'},
    {key: 'plan', value: 'plan', text: 'Plan'},
    {key: 'subplan', value: 'subplan', text: 'SubPlan'},
    {key: 'class', value: 'class', text: 'Class'},
    {key: 'subclass', value: 'subclass', text: 'SubClass'},
    {key: 'sequence', value: 'sequence', text: 'Sequence'},
    {key: 'rxbin', value: 'rxbin', text: 'RX BIN'},
    {key: 'rxpcn', value: 'rxpcn', text: 'RX PCN'},
    {key: 'rxid', value: 'rxid', text: 'RX Id'},
    {key: 'rxgroup', value: 'rxgroup', text: 'RX Group'},

]

const codeSystems = [
    {key: 'hcpcs', value: 'https://bluebutton.cms.gov/resources/codesystem/hcpcs', text: 'HCPCS'},
    {key: 'cpt', value: 'http://www.ama-assn.org/go/cpt', text: 'CPT'},
    {key: 'rxnorm', value: 'http://www.nlm.nih.gov/research/umls/rxnorm', text: 'RxNorm'},
]

const hcpcsCodes = [
    {key: 'A0426', value: 'A0426', text: 'A0426'},
    {key: 'A5500', value: 'A5500', text: 'A5500'},
    {key: 'E0110', value: 'E0110', text: 'E0110'},
    {key: 'E0130', value: 'E0130', text: 'E0130'},
    {key: 'E0250', value: 'E0250', text: 'E0250'},
    {key: 'E0424', value: 'E0424', text: 'E0424'},
    {key: 'E0431', value: 'E0431', text: 'E0431'},
    {key: 'E0433', value: 'E0433', text: 'E0433'},
    {key: 'E0434', value: 'E0434', text: 'E0434'},
    {key: 'E0439', value: 'E0439', text: 'E0439'},
    {key: 'E0441', value: 'E0441', text: 'E0441'},
    {key: 'E0442', value: 'E0442', text: 'E0442'},
    {key: 'E0443', value: 'E0443', text: 'E0443'},
    {key: 'E0444', value: 'E0444', text: 'E0444'},
    {key: 'E0465', value: 'E0465', text: 'E0465'},
    {key: 'E0470', value: 'E0470', text: 'E0470'},
    {key: 'E0601', value: 'E0601', text: 'E0601'},
    {key: 'E1390', value: 'E1390', text: 'E1390'},
    {key: 'E1391', value: 'E1391', text: 'E1391'},
    {key: 'E1392', value: 'E1392', text: 'E1392'},
    {key: 'K0738', value: 'K0738', text: 'K0738'},
]

const cptCodes = [
    {key: '82947', value: '82947', text: '82947'},
    {key: '94649', value: '94649', text: '94649'},
    {key: '94660', value: '94660', text: '94660'},
    {key: '95259', value: '95259', text: '95259'},
    {key: '97542', value: '97542', text: '97542'},
]

const rxNormCodes = [
    {key: '209431', value: '209431', text: '209431'},
    {key: '860195', value: '860195', text: '860195'},
]

const organizations = [
    {key: 'medicare', value: 'Centers for Medicare and Medicaid Services', text: 'Centers for Medicare and Medicaid Services'},
]
module.exports = {grant_type, stateOptions, coverageCodes, codeSystems, hcpcsCodes, cptCodes, rxNormCodes, organizations};
