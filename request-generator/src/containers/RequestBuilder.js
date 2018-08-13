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
            response:null
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

    submit_info(){
        const birthYear = 2018-parseInt(this.state.age,10);
        let json_request = {
            resourceType: "Parameters",
            parameter: [
              {
                name: "request",
                part: [
                  {
                    name: "eligibilityrequest",
                    resource: {
                      resourceType: "EligibilityRequest",
                      id: "050de4e5-1e53-44bd-9422-5c54caaf3958",
                      meta: {
                        profile: [
                          "http://base.url/DaVinciEligibilityRequest"
                        ]
                      },
                      extension: [
                        {
                          url: "http://hl7.org/davinci/crd/eligibilityRequest/serviceInformation",
                          extension: [
                            {
                              url: "http://hl7.org/davinci/crd/eligibilityRequest/serviceInformation/serviceRequestType",
                              valueCodeableConcept: {
                                coding: [
                                  {
                                    system: "http://www.ama-assn.org/go/cpt",
                                    code: this.state.code,
                                    display: "Walker"
                                  }
                                ]
                              }
                            },
                            {
                              url: "http://hl7.org/davinci/crd/eligibilityRequest/serviceInformation/patientContext",
                              valueReference: {
                                reference: "urn:uuid:d1332d6b-4d62-434c-852d-cfcef09a3973"
                              }
                            }
                          ]
                        }
                      ],
                      status: "active",
                      patient: {
                        reference: "urn:uuid:60da0410-fd52-48c4-ae1d-27af9fdcb4eb"
                      },
                      provider: {
                        reference: "urn:uuid:2bc6c647-3d66-46b1-a6a4-35d877242a8c"
                      },
                      insurer: {
                        reference: "urn:uuid:6596481f-66a3-400e-994d-230a489c8f7c"
                      },
                      facility: {
                        reference: "urn:uuid:9feebcb0-db55-44a1-9b6d-af5fc57e6afe"
                      },
                      coverage: {
                        reference: "urn:uuid:ccff886d-7dbc-470f-b8df-4d9a9bd4c598"
                      }
                    }
                  },
                  {
                    name: "patient",
                    resource: {
                      resourceType: "Patient",
                      id: "60da0410-fd52-48c4-ae1d-27af9fdcb4eb",
                      gender: this.state.gender,
                      birthDate: birthYear + "-07-04"
                    }
                  },
                  {
                    name: "coverage",
                    resource: {
                      resourceType: "Coverage",
                      id: "ccff886d-7dbc-470f-b8df-4d9a9bd4c598",
                      class: [
                        {
                          type: {
                            system: "http://hl7.org/fhir/coverage-class",
                            code: "plan"
                          },
                          value: "Medicare Part D"
                        }
                      ]
                    }
                  },
                  {
                    name: "provider",
                    resource: {
                      resourceType: "Practitioner",
                      id: "2bc6c647-3d66-46b1-a6a4-35d877242a8c",
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
                  },
                  {
                    name: "insurer",
                    resource: {
                      resourceType: "Organization",
                      id: "6596481f-66a3-400e-994d-230a489c8f7c",
                      name: "Centers for Medicare and Medicaid Services"
                    }
                  },
                  {
                    name: "facility",
                    resource: {
                      resourceType: "Location",
                      id: "9feebcb0-db55-44a1-9b6d-af5fc57e6afe",
                      address: {
                        line: [
                          "100 Good St"
                        ],
                        city: "Bedford",
                        state: "MA",
                        postalCode: "01730"
                      }
                    }
                  },
                  {
                    name: "patientContext",
                    resource: {
                      resourceType: "Condition",
                      id: "d1332d6b-4d62-434c-852d-cfcef09a3973",
                      clinicalStatus: "active",
                      code: {
                        coding: [
                          {
                            system: "http://hl7.org/fhir/sid/icd-10",
                            code: "M23.51",
                            display: "Instability of the right knee"
                          }
                        ]
                      },
                      subject: {
                        reference: "urn:uuid:60da0410-fd52-48c4-ae1d-27af9fdcb4eb"
                      }
                    }
                  }
                ]
              },
              {
                name: "endpoint"
              },
              {
                name: "requestQualification"
              }
            ]
          }
        const tokenUrl = "http://localhost:8180/auth/realms/SpringBootKeycloak/protocol/openid-connect/token"
        // Make a request   
        // (async () => {
        //     const rawResponse = await fetch("http://localhost:8080/api/testing/", {
        //       method: "POST",
        //       headers: {
        //         "Accept": "application/json",
        //         "Content-Type": "application/json"
        //       },
        //       body: JSON.stringify({a: 7, str: 'Some string: &=&'})
        //     });
        //     var content = await rawResponse.text();
        //     console.log(content);
        //   })();

        let params = {
            grant_type:"password",
            username:"user1",
            password:"password",
            client_id:"dme-app-login"
        }

        // Encodes the params to be compliant with
        // x-www-form-urlencoded content type.
        const searchParams = Object.keys(params).map((key) => {
            return encodeURIComponent(key) + '=' + encodeURIComponent(params[key]);
          }).join('&');
          
        (async () => {
            // We get the token from the url
            // const tokenResponse = await fetch(tokenUrl, {
            //     method: "POST",
            //     headers: {
            //         "Content-Type":"application/x-www-form-urlencoded"
            //     },
            //     body: searchParams
            // }).then((response) =>{
            //     return response.json();
            // });
            // const token = "bearer " + tokenResponse.access_token;
            // console.log(token);
            // const rawResponse = await fetch("http://localhost:8080/api/data/1/", {
            //   method: "GET",
            //   headers: {
            //     "Content-Type": "application/x-www-form-urlencoded",
            //     "Authorization": token
            //   }
            // });
            // var content = await rawResponse.text();
            // console.log(content);

            const fhirResponse = await fetch("http://localhost:8080/fhir/$coverage-requirements-discovery/",{
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(json_request)
            }).then(response => {
                return response.json();
            });
            var extra = JSON.stringify(fhirResponse.parameter[0].part)

            this.setState({response: extra});
          })();

  
          


        // fetch('http://localhost:8080/api/testing', {
        //     method: 'post',
        //     headers: {
        //       'Accept': 'application/json, text/plain, */*',
        //       'Content-Type': 'application/json'
        //     },
        //     body: JSON.stringify({a: 7, str: 'Some string: &=&'})
        //   }).then(res => console.log(res));

    
    }

    render() {
        
        return (
            <div>
            <div className="form-group container left-form">
                {Object.keys(this.state)
                .map((key) => {

                    if(key!=="response"){
                        return <div key={key}> 
                        <InputBox
                            elementName={key} 
                            updateCB={this.updateStateElement}/>
                        <br />
                    </div>

                    }

                })}

                <button className="btn btn-class" onClick={this.submit_info}>Submit</button>
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
