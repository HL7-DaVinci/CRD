import { getBaseUrl } from '../../Utils';
import React, { Component } from 'react';
import {getUrlParameter, makeUrlParams} from './url';
import {grant_type} from './constants';
import { Dropdown } from 'semantic-ui-react';
import PatientInfo from './Resources/PatientInfo';
import CoverageInfo from './Resources/CoverageInfo';
import DisplayBox from './DisplayBox/DisplayBox';
import Expandable from './Expandable';
import FHIR from "fhirclient"
import buildRequest from './buildRequest';

import './launch.css';
import PractitionerInfo from './Resources/PractitionerInfo';
import DeviceRequestInfo from './Resources/DeviceRequestInfo';
export default class Index extends Component {
    constructor(props) {
        super(props);
        this.state = {
            token: '',
            params: {},
            patients: [],
            // should make these resources a mutable list
            patient: {
                "resourceType": "Patient",
                "id": "example1",
            },
            coverage: {
                "resourceType": "Coverage",
                "id": "example2",
                "beneficiary": {
                    "reference": "Patient/example1"
                },
                "status": "active",
                "class": [],
                "payor": [
                    {"reference": "Organization/covOrg1"}
                ]
            },
            coverageOrg: {
                "resourceType": "Organization",
                "id": "covOrg1",
            },
            practitioner: {
                "resourceType": "Practitioner",
                "id": "example3",
            },
            practitionerRole: {
                "resourceType": "PractitionerRole",
                "id": "role1",
                "practitioner": {
                  "reference": "Practitioner/example3"
                },
                "location": [
                  {
                    "reference": "Location/loc1"
                  }
                ],
                "organization ": {
                  "reference": "Organization/pracOrg1"
                }
            },
            practitionerLocation: {
                "resourceType": "Location",
                "id": "loc1",
                "address": {
                  "line": [
                    "100 Good St"
                  ],
                  "city": "Bedford",
                  "postalCode": "01730"
                }
            },
            practitionerOrg: {
                "resourceType": "Organization",
                "id": "pracOrg1",
            },
            deviceRequest: {
                    "resourceType": "DeviceRequest",
                    "id": "example4",
                    "meta": {
                      "profile": [
                        "http://hl7.org/fhir/us/davinci-crd/R4/StructureDefinition/profile-devicerequest-r4"
                      ]
                    },
                    "insurance": [
                      {
                        "reference": "Coverage/example2"
                      }
                    ],
                    "status": "draft",

                    "subject": {
                      "reference": "Patient/example1"
                    },
                    "performer": {
                      "reference": "Practitioner/example3"
                    },
                    "intent": "original-order",
                  
            },
        };

        this.handleFieldChange = this.handleFieldChange.bind(this);
        this.makePatientResource = this.makePatientResource.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleFieldChange(resource, fieldId, value){
        if(fieldId === 'resource') {
            this.setState({
                [resource]: value
            })
        } else {
            this.setState(prevState => ({
                [resource]: {                   // object that we want to update
                    ...prevState[resource],    // keep all other key-value pairs
                    [fieldId]: value         // update the value of specific key
                }
            }))
        }
      };

    componentDidMount() {
        const state = getUrlParameter("state");
        const code = getUrlParameter("code");
        const { tokenUri, client_id, serviceUri, redirect_uri } = JSON.parse(sessionStorage[state]);
        this.setState({serviceUri});
        const data = {code, grant_type, redirect_uri, client_id};
        const body = makeUrlParams(data);
        this.makeTokenRequest(tokenUri, body, serviceUri);
    }

    handleSubmit() {
        let deviceRequestResources = [
            this.state.patient,
            this.state.deviceRequest,
            this.state.coverage,
            this.state.practitioner,
            this.state.practitionerRole,
            this.state.practitionerLocation,
            this.state.practitionerOrg,
            this.state.coverageOrg,
          ];
          deviceRequestResources = deviceRequestResources.map((e) => {
              return {resource: e}
          })
          const request = buildRequest(this.state.deviceRequest, this.state.patient, this.state.serviceUri, this.state.token, deviceRequestResources, true, 'order-sign')
          fetch('http://localhost:8090/r4/cds-services/order-sign-crd', {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(request)
        }).then(response => {
            response.json().then((fhirResponse) => {
                console.log(fhirResponse);
                if (fhirResponse && fhirResponse.status) {
                    console.log(fhirResponse);
                } else {
                    console.log(fhirResponse);
                    this.setState({ response: fhirResponse });
                }
            })
        }).catch((err) => console.log(err));
    }

    makeTokenRequest(tokenUri, body, serviceUri) {
        fetch(tokenUri, {
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            method: "POST",
            body: body
        })
        .then(response => response.json())
        .then((data) => {
            const token = data.access_token;
            this.setState({token});
            const patients = fetch(`${serviceUri}/Patient`, {
                headers: {
                    "Authorization": `Bearer ${token}`
                }
            }).then(response => response.json()).then((data) => {
                this.setState({patients: data.entry})
            })
        })
    }



    makePatientResource() {
          const patientResource = {...this.state.patient};

            patientResource.gender = this.state.patient.gender;
            patientResource.birthDate = this.getApproxBirthdate(this.state.patient.age)
            patientResource.address = [
                {
                    "use": "home",
                    "type": "both",
                    "state": this.state.patient.state
                }
          ]
          return patientResource;
          
    }

    renderPatients(patients) {
        console.log("hello");
        const options = patients.map((entry) => {
            return {
                key: entry.fullUrl,
                value: entry.resource.id,
                text: entry.resource.id
            }
        })
        return  (<Dropdown 
        placeholder='Select client'
        fluid
        search
        selection
        options={options}
    />)
    }

    render() {
        return (
            <div>
                <div className="left">
                    <Expandable title="Patient" Fragment={<PatientInfo callback={this.handleFieldChange} patient={this.state.patient}/>} />
                    <Expandable title="Coverage" Fragment={<CoverageInfo callback={this.handleFieldChange} coverage={this.state.coverage} organization={this.state.coverageOrg}/>} />
                    <Expandable title="Practitioner" Fragment={<PractitionerInfo callback={this.handleFieldChange} practitioner={this.state.practitioner} role={this.state.practitionerRole} org={this.state.practitionerOrg} location={this.state.practitionerLocation}/>} />
                    <Expandable title="Device Request" Fragment={<DeviceRequestInfo callback={this.handleFieldChange} deviceRequest={this.state.deviceRequest}/>} />

                    <button className='submitButton' onClick={this.handleSubmit}>Submit</button>
                </div>
                <div className="right">
                    <DisplayBox 
                      isDemoCard={true}
                      response={this.state.response}

                    />
                </div>
            </div>
        )
    }
}
