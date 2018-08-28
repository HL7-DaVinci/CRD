import React, {Component} from 'react';
import {connect} from 'react-redux';
import { bindActionCreators} from 'redux';
import { fetchCrdResponse } from '../actions';
import InputBox from '../components/InputBox';
import DisplayBox from '../components/DisplayBox';
import '../index.css';

class RequestBuilder extends Component{
    constructor(props){
        super(props);
        this.state = { 
            age: null,
            gender: null,
            code: null,
            response:null,
            token: null
        };

        this.validateMap={
            age:(foo=>{return isNaN(foo)}),
            gender:(foo=>{return foo!=="male" && foo!=="female"}),
            code:(foo=>{return !foo.match(/^[a-z0-9]+$/i)})
        };


        
    this.updateStateElement = this.updateStateElement.bind(this);
    this.submit_info = this.submit_info.bind(this);



    }

    updateStateElement = (elementName,text) => {
        this.setState({ [elementName]: text});
        }

    onInputChange(event){
        this.setState({ [event.target.name]: event.target.value });
    }

    login(){
        const tokenUrl = "http://localhost:8180/auth/realms/ClientFhirServer/protocol/openid-connect/token"
        let params = {
            grant_type:"password",
            username:"user1",
            password:"password",
            client_id:"app-login"
        }

        // Encodes the params to be compliant with
        // x-www-form-urlencoded content type.
        const searchParams = Object.keys(params).map((key) => {
            return encodeURIComponent(key) + '=' + encodeURIComponent(params[key]);
          }).join('&');
          
        return (async () => {
            // We get the token from the url
            const tokenResponse = await fetch(tokenUrl, {
                method: "POST",
                headers: {
                    "Content-Type":"application/x-www-form-urlencoded"
                },
                body: searchParams
            }).then((response) =>{
                return response.json();
            });
            console.log(tokenResponse);
            const token = tokenResponse.access_token;
            this.setState({token})
        })();
    }

    submit_info(){
        const birthYear = 2018-parseInt(this.state.age,10);
        // let json_request = {
        //     hookInstance: "d1577c69-dfbe-44ad-ba6d-3e05e953b2ea",
        //     fhirServer: "http://hooks.smarthealthit.org:9080",
        //     hook: "order-review",
        //     fhirAuthorization: {
        //       access_token: "some-opaque-fhir-access-token",
        //       token_type: "Bearer",
        //       expires_in: 300,
        //       scope: "patient/Patient.read patient/Observation.read",
        //       subject: "cds-service4"
        //     },
        //     user: "Practitioner/example",
        //     context: {
        //       patientId: "1288992",
        //       encounterId: "89284",
        //       orders: {
        //         resourceType: "Bundle",
        //         entry: [
        //           {
        //             resource: {
        //               resourceType: "DeviceRequest",
        //               note: [
        //                 {
        //                   text: "a sample device request"
        //                 }
        //               ]
        //             }
        //           }
        //         ]
        //       }
        //     },
        //     prefetch: {
        //       patient: {
        //         resourceType: "Patient",
        //         gender: this.state.gender,
        //         birthDate: birthYear + "-12-23",
        //         id: "1288992",
        //         active: true
        //       }
        //     }
        // };
        

        (async () => {
        await this.login();
        let json_request = {
            hookInstance: "d1577c69-dfbe-44ad-ba6d-3e05e953b2ea",
            fhirServer: "http://localhost:8080/fhir-server",
            hook: "order-review",
            oauth : {
              "access_token" : this.state.token,
              "token_type" : "Bearer",
              "expires_in" : 300,
              "scope" : "patient/Patient.read patient/Observation.read",
              "subject" : "cds-service4"
            },
            user: "Practitioner/example",
            context: {
              patientId: "1234",
              encounterId: "89284",
              orders: {
                resourceType: "Bundle",
                entry: [
                  {
                    resource: {
                      resourceType: "DeviceRequest",
                      status: "draft",
                      codeCodeableConcept: {
                        coding: [
                          {
                            system: "https://bluebutton.cms.gov/resources/codesystem/hcpcs",
                            code: "E0424"
                          }
                        ],
                        text: "Stationary Compressed Gaseous Oxygen System, Rental"
                      },
                      subject: {
                        reference: "Patient/12"
                      },
                      authoredOn: "2018-08-08",
                      insurance: [{
                        reference: "Coverage/1234"
                      }],
                      performer: {
                        reference: "PractitionerRole/1234"
                      }
                    }
                  }
                ]
              }
            },
            prefetch: {
              patient: {
                resourceType: "Patient",
                gender: "male",
                birthDate: "1970-07-04"
              },
              coverage: {
                resourceType: "Coverage",
                id: "1234",
                class: [
                  {
                    type: {
                      system: "http://hl7.org/fhir/coverage-class",
                      code: "plan"
                    },
                    value: "Medicare Part D"
                  }
                ],
                payor: [
                  {
                    reference: "Organization/e182fb07-e8c4-4cc0-8710-94f8b3a17b0b"
                  }
                ]
              },
              location: {
                resourceType: "Location",
                id: "89abea45-75d5-4730-a214-027fcb903ca1",
                address: {
                  line: [
                    "100 Good St"
                  ],
                  city: "Bedford",
                  state: "MA",
                  postalCode: "01730"
                }
              },
              practitionerRole: {
                resourceType: "PractitionerRole",
                practitioner: {
                  reference: "Practitioner/13608725-a5f5-4276-b44a-1fe2c7273555"
                },
                location: [
                  {
                    reference: "Location/89abea45-75d5-4730-a214-027fcb903ca1"
                  }
                ]
              },
              insurer: {
                resourceType: "Organization",
                id: "e182fb07-e8c4-4cc0-8710-94f8b3a17b0b",
                name: "Centers for Medicare and Medicaid Services"
              },
              provider: {
                resourceType: "Practitioner",
                id: "13608725-a5f5-4276-b44a-1fe2c7273555",
                identifier: [
                  {
                    system: "http://hl7.org/fhir/sid/us-npi",
                    value: "1122334455"
                  }
                ],
                name: [
                  {
                    family: "Doe",
                    given: [
                      "Jane"
                    ],
                    prefix: [
                      "Dr."
                    ]
                  }
                ]
              }
            }
          };

            const fhirResponse = await fetch("http://localhost:8090/cds-services/coverage-requirements-discovery",{
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(json_request)
            }).then(response => {
                return response.json();
            });
            console.log(fhirResponse);
            // hand jam a card with actual contents in to test 
            // how it looks 
            var extra = {
                summary: "Example Card",
                indicator: "info",
                detail: "Add an XYZ complimentary medication OR switch patient order to ABC. See SMART app for more details.",
                source: {
                  label: "Medicine Library",
                  url: "https://example.com"
                },
                links: [
                  {
                    label: "Medication SMART app",
                    url: "https://example.com/launch",
                    type: "smart"
                  }
                ],
                suggestions: [
                  {
                    label: "Add Complimentary",
                    uuid: "123",
                    actions: [
                      {
                        type: "create",
                        description: "Add XYZ",
                        resource: {}
                      },
                      {
                        type: "delete",
                        description: "Cancel ABC",
                        resource: "MedicationRequest/ABC"
                      }
                    ]
                  },
                  {
                    label: "Change Order",
                    uuid: "456",
                    actions: [
                      {
                        type: "modify",
                        description: "Modify dosage of Medication",
                        resource: "MedicationRequest/ABC"
                      }
                    ]
                  }
                ]
              }
            fhirResponse.cards.push(extra);
            this.setState({response: fhirResponse});
          })();  
    }

    validateState(){
        const validationResult = {};
        Object.keys(this.validateMap).forEach(key => {
            if(this.state[key] && this.validateMap[key](this.state[key])){
                // Basically we want to know if we have any errors 
                // or empty fields, and we want the errors to override
                // the empty fields, so we make errors 0 and unpopulated
                // fields 2.  Then we just look at whether the product of all
                // the validations is 0 (error), 1 (valid) , or >1 (some unpopulated fields).  
                validationResult[key]=0;
            }else if(this.state[key]){
                // the field is populated and valid
                validationResult[key]=1;
            }else{
                // the field is not populated
                validationResult[key]=2
            }
        });
        return validationResult;
    }

    render() {

        const validationResult = this.validateState();
        const total = Object.keys(validationResult).reduce((previous,current) =>{
            return validationResult[current]*previous
        },1);

        return (
            <div>
            <div className="form-group container left-form">
                {Object.keys(this.validateMap)
                .map((key) => {

                    if(key!=="response" && key!=="validateMap"){
                        return <div key={key}> 

                        <InputBox
                            elementName={key} 
                            updateCB={this.updateStateElement}
                            extraClass={!validationResult[key] ? "error-border" : "regular-border"}/>
                        <br />
                    </div>

                    }

                })}

                <button className={"btn btn-class "+ (!total ? "button-error" : total===1 ? "button-ready":"button-empty-fields")} onClick={this.submit_info}>Submit</button>
            </div>
            <div className="right-form">
                <DisplayBox 
                response = {this.state.response}/>    
            </div>
            </div>


           


        )
    }
}
function mapDispatchToProps(dispatch) {
    return bindActionCreators({fetchCrdResponse},dispatch);
}
export default connect(null,mapDispatchToProps)(RequestBuilder);
