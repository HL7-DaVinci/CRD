import React, {Component} from 'react';

import InputBox from '../components/InputBox';
import Toggle from '../components/Toggle';
import DisplayBox from '../components/DisplayBox';
import DropdownInput from '../components/DropdownInput';
import CheckBox from '../components/CheckBox';
import ConsoleBox from '../components/ConsoleBox';
import '../index.css';
import '../components/consoleBox.css';
import Loader from 'react-loader-spinner';
import config from '../properties.json';


const types = {
  error: "errorClass",
  info: "infoClass",
  debug: "debugClass",
  warning: "warningClass"
}
export default class RequestBuilder extends Component{
    constructor(props){
        super(props);
        this.state = { 
            age: null,
            gender: null,
            code: null,
            response:null,
            token: null,
            oauth:false,
            loading:false,
            logs:[]
        };

        this.validateMap={
            age:(foo=>{return isNaN(foo)}),
            gender:(foo=>{return foo!=="male" && foo!=="female"}),
            code:(foo=>{return !foo.match(/^[a-z0-9]+$/i)})
        };


        
    this.updateStateElement = this.updateStateElement.bind(this);
    this.submit_info = this.submit_info.bind(this);
    this.consoleLog = this.consoleLog.bind(this);

    }

    consoleLog(content, type){
      let jsonContent = {
        content: content,
        type: type
      }
      this.setState(prevState => ({
        logs: [...prevState.logs, jsonContent]
      }))
    }

    updateStateElement = (elementName,text) => {
        this.setState({ [elementName]: text});
        }

    onInputChange(event){
        this.setState({ [event.target.name]: event.target.value });
    }

    async login(){

      const tokenUrl = "http://localhost:8180/auth/realms/"+config.realm+"/protocol/openid-connect/token"
      this.consoleLog("Retrieving OAuth token from "+tokenUrl,types.info);
      let params = {
          grant_type:"password",
          username:"user1",
          password:"password",
          client_id:config.client
      }
      if(config.client){
        this.consoleLog("Using client {" + config.client + "}",types.info)
      }else{
        this.consoleLog("No client id provided in properties.json",this.warning);
      }

      // Encodes the params to be compliant with
      // x-www-form-urlencoded content type.
      const searchParams = Object.keys(params).map((key) => {
          return encodeURIComponent(key) + '=' + encodeURIComponent(params[key]);
        }).join('&');

      // We get the token from the url

      const tokenResponse = await fetch(tokenUrl, {
        method: "POST",
        headers: {
            "Content-Type":"application/x-www-form-urlencoded"
        },
        body: searchParams
      }).then((response) =>{
          return response.json();
      }).then(response=>{
        console.log(response);
          const token = response?response.access_token:null;
          if(token){
            this.consoleLog("Successfully retrieved token",types.info);
          }else{
            this.consoleLog("Failed to get token",types.warning);
            if(response.error_description){
              this.consoleLog(response.error_description,types.error);
            }
          }

          this.setState({token})
          return token;

      }).catch(reason =>{
        this.consoleLog("Failed to get token", types.error);
        this.consoleLog("Bad request");
      });

      return tokenResponse;

    }

    async submit_info(){
      this.consoleLog("Initiating form submission",types.info);
      if(this.state.oauth){
        const token = await this.login();
      }
      let json_request = this.getJson(1);

      this.setState({loading:true});
          this.consoleLog("Fetching response from http://localhost:8090/cds-services/order-review-crd/",types.info)
          try{
            const fhirResponse= await fetch("http://localhost:8090/cds-services/order-review-crd/",{
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(json_request)
            }).then(response => {
              this.consoleLog("Recieved response",types.info);
                return response.json();
            }).catch(reason => this.consoleLog("No response recieved from the server", types.error));

            if(fhirResponse && fhirResponse.status){
              console.log(fhirResponse);
              this.consoleLog("Server returned status " 
                              + fhirResponse.status + ": " 
                              + fhirResponse.error,types.error);
              this.consoleLog(fhirResponse.message,types.error);
            }else{
              this.setState({response: fhirResponse});
            }
          this.setState({loading:false});
          }catch(error){
            this.setState({loading:false});
            this.consoleLog("Unexpected error occured",types.error)
            // this.consoleLog(e.,types.error);
            if(error instanceof TypeError){
              this.consoleLog(error.name + ": " + error.message,types.error);
            }
          }

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

      const options = {
        option1:{
          text:"Male",
          value:"male"
        },
        option2:{
          text:"Female",
          value:"female"
        }
      }
        const validationResult = this.validateState();
        const total = Object.keys(validationResult).reduce((previous,current) =>{
            return validationResult[current]*previous
        },1);

        return (
            <div>
            <div className="form-group container left-form">
                {Object.keys(this.validateMap)
                .map((key) => {

                  // Make type of input and the associated options available in some
                  // top level json instead of hard-coding the if-else per key
                  // e.g., gender should have a "toggle" attribute and the options
                  // it wants should be written in the JSON.  This way if we want more
                  // options later they're easy to add.
                    if(key!=="response" && key!=="validateMap"){
                      if(key==="gender"){
                        return <div key={key}>
                        <div className="header">
                          Gender
                        </div>
                        <Toggle
                        elementName={key}
                        updateCB={this.updateStateElement}
                        options={options}
                        extraClass={!validationResult[key] ? "error-border" : "regular-border"}
                        ></Toggle>
                        <br />
                        </div>

                      }else if(key==="code"){
                        return <div key={key}>
                        <div className="header">
                          Code
                        </div>
                        <DropdownInput
                            elementName={key}
                            updateCB={this.updateStateElement}
                            />

                          <br />
                          </div>
                      }else{
                        return <div key={key}>
                        <div className="header">
                          Age
                        </div>
                        <InputBox
                            elementName={key}
                            updateCB={this.updateStateElement}
                            extraClass={!validationResult[key] ? "error-border" : "regular-border"}/>
                          <br />
                          </div>
                      }


                    }

                })}

                <br />
                <button className={"submit-btn btn btn-class "+ (!total ? "button-error" : total===1 ? "button-ready":"button-empty-fields")} onClick={this.submit_info}>Submit

                </button>


                <CheckBox elementName="oauth" updateCB={this.updateStateElement}/>

                <div id="fse" className={"spinner " + (this.state.loading?"visible":"invisible")}>
                <Loader
                  type="Oval"
                  color="#222222"
                  height="16"
                  width="16"
                />
                </div>

                <ConsoleBox logs ={this.state.logs}/>
            </div>

            <div className="right-form">
                <DisplayBox
                response = {this.state.response}/>
            </div>

            </div>





        )
    }

    getJson(number){
      const birthYear = 2018-parseInt(this.state.age,10);
      if(number===1){
        return  {
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
            patientId: "12",
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
                          code: this.state.code
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
            deviceRequestBundle: {
              resourceType: "Bundle",
              type: "collection",
              entry: [
                {
                  resource: {
                    resourceType: "DeviceRequest",
                    status: "draft",
                    codeCodeableConcept: {
                      coding: [
                        {
                          system: "https://bluebutton.cms.gov/resources/codesystem/hcpcs",
                          code: this.state.code
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
                },
                {
                  resource: {
                    id: "12",
                    resourceType: "Patient",
                    gender: this.state.gender,
                    birthDate: birthYear + "-12-23"
                  }
                },
                {
                  resource: {
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
                  }
                },
                {
                  resource: {
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
                  }
                },
                {
                  resource: {
                    resourceType: "PractitionerRole",
                    practitioner: {
                      reference: "Practitioner/13608725-a5f5-4276-b44a-1fe2c7273555"
                    },
                    location: [
                      {
                        reference: "Location/89abea45-75d5-4730-a214-027fcb903ca1"
                      }
                    ]
                  }
                },
                {
                  resource: {
                    resourceType: "Organization",
                    id: "e182fb07-e8c4-4cc0-8710-94f8b3a17b0b",
                    name: "Centers for Medicare and Medicaid Services"
                  }
                },
                {
                  resource: {
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
              ]
            }
          }
        };

      }else if(number===2){
        return {
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
      }

    }
}



