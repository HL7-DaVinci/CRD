import React, {Component} from 'react';

import InputBox from '../components/InputBox';
import Toggle from '../components/Toggle';
import DisplayBox from '../components/DisplayBox';
import DropdownInput from '../components/DropdownInput';
import DropdownState from '../components/DropdownState';
import CheckBox from '../components/CheckBox';
import ConsoleBox from '../components/ConsoleBox';
import '../index.css';
import '../components/consoleBox.css';
import Loader from 'react-loader-spinner';
import config from '../properties.json';
import KJUR, {KEYUTIL} from 'jsrsasign';

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
            patientState: null,
            practitionerState: null,
            response:null,
            token: null,
            oauth:false,
            loading:false,
            logs:[],
            keypair:KEYUTIL.generateKeypair('RSA',2048)
        };
        this.validateMap={
            age:(foo=>{return isNaN(foo)}),
            gender:(foo=>{return foo!=="male" && foo!=="female"}),
            code:(foo=>{return !foo.match(/^[a-z0-9]+$/i)})
        };
        console.log(this.state.keypair);




        
    this.updateStateElement = this.updateStateElement.bind(this);
    this.startLoading = this.startLoading.bind(this);
    this.submit_info = this.submit_info.bind(this);
    this.consoleLog = this.consoleLog.bind(this);

    }
  
    makeid() {
      var text = [];
      var possible = "---ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
      for (var i = 0; i < 25; i++)
        text.push(possible.charAt(Math.floor(Math.random() * possible.length)));
    
      return text.join('');
    }

    async createJwt(){
      var pubKey = this.state.keypair.pubKeyObj;
    
      const jwkPrv2 = KEYUTIL.getJWKFromKey(this.state.keypair.prvKeyObj);
      const jwkPub2 = KEYUTIL.getJWKFromKey(this.state.keypair.pubKeyObj);
      console.log(pubKey);
      const currentTime = KJUR.jws.IntDate.get('now');
      const endTime = KJUR.jws.IntDate.get('now + 1day');
      const kid = KJUR.jws.JWS.getJWKthumbprint(jwkPub2)
      // const pubPem = {"pem":KEYUTIL.getPEM(pubKey),"id":kid};
      const pubPem = {"pem":jwkPub2,"id":kid};

      // Check if the public key is already in the db
      const checkForPublic = await fetch("http://localhost:3001/public_keys?id="+kid,{
        "headers":{
          "Content-Type":"application/json"
        },
        "method":"GET"
      }).then(response => {return response.json()});
      if(!checkForPublic.length){
        // POST key to db if it's not already there
        const alag = await fetch("http://localhost:3001/public_keys",{
          "body": JSON.stringify(pubPem),
          "headers":{
            "Content-Type":"application/json"
          },
          "method":"POST"
        });
      }
      const header = {
        "alg":"RS256",
        "typ":"JWT",
        "kid":kid,
        "jku":"http://localhost:3001/public_keys"
      };
      const body = {
        "iss":"localhost:3000",
        "aud":"r4/order-review-services",
        "iat": currentTime,
        "exp": endTime,
        "jti": this.makeid()
      }
      
      var sJWT = KJUR.jws.JWS.sign("RS256",JSON.stringify(header),JSON.stringify(body),jwkPrv2)
      
      return sJWT;
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

    updateStateElement = (elementName, text) => {
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
    startLoading(){
      this.setState({loading:true}, ()=>{
        this.submit_info();
      });
      
    }
    async submit_info(){
      this.consoleLog("Initiating form submission",types.info);

      if(this.state.oauth){
        const token = await this.login();
      }
      let json_request = this.getJson();
      console.log(json_request);
      let jwt = await this.createJwt();
      //console.log(jwt);
      jwt = "Bearer " + jwt;
      var myHeaders = new Headers({
        "Content-Type": "application/json",
        "authorization": jwt
      });
            this.consoleLog("Fetching response from http://localhost:8090/r4/cds-services/order-review-crd/",types.info)
          try{
            const fhirResponse= await fetch("http://localhost:8090/r4/cds-services/order-review-crd",{
                method: "POST",
                headers: myHeaders,
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
                })}
                <div>
                  <div className="leftStateInput">
                  <div className="header">
                          Patient State
                  </div>
                  <DropdownState 
                    elementName="patientState"
                    updateCB={this.updateStateElement}
                  />
                  </div>
                  <div className="rightStateInput">
                  <div className="header">
                          Practitioner State
                        </div>
                  <DropdownState 
                    elementName="practitionerState"
                    updateCB={this.updateStateElement}
                  />
                  </div>
                </div>


                <br />
                <button className={"submit-btn btn btn-class "+ (!total ? "button-error" : total===1 ? "button-ready":"button-empty-fields")} onClick={this.startLoading}>Submit

                </button>


                <CheckBox elementName="oauth" displayName="OAuth" updateCB={this.updateStateElement}/>
                <CheckBox elementName="prefetch" displayName="Include Prefetch" updateCB={this.updateStateElement}/>
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

    getJson(){
      const birthYear = 2018-parseInt(this.state.age,10);
      let request = {
        hookInstance: "d1577c69-dfbe-44ad-ba6d-3e05e953b2ea",
        fhirServer: "http://localhost:8080/ehr-server/r4/",
        hook: "order-review",
        fhirAuthorization : {
          "access_token" : this.state.token,
          "token_type" : "Bearer",
          "expires_in" : 300,
          "scope" : "patient/Patient.read patient/Observation.read",
          "subject" : "cds-service4"
        },
        user: "Practitioner/example",
        context: {
          patientId: "pat1234",
          encounterId: "enc89284",
          orders: {
            resourceType: "Bundle",
            entry: [
              {
                resource: {
                  id: "a24439",
                  resourceType: "DeviceRequest",
                  id: "devreq1234",
                  status: "draft",
                  codeCodeableConcept: {
                    coding: [
                      {
                        system: this.state.codeSystem,
                        code: this.state.code
                      }
                    ]
                  },
                  subject: {
                    reference: "Patient/pat1234"
                  },
                  authoredOn: "2018-08-08",
                  insurance: [{
                    reference: "Coverage/cov1234"
                  }],
                  performer: {
                    reference: "PractitionerRole/prarol1234"
                  }
                }
              }
            ]
          }
        }
      };
      if (this.state.prefetch) {
        request.prefetch = {
          deviceRequestBundle: {
            resourceType: "Bundle",
            type: "collection",
            entry: [
              {
                resource: {
                  resourceType: "DeviceRequest",
                  id: "devreq1234",
                  status: "draft",
                  codeCodeableConcept: {
                    coding: [
                      {
                        system: this.state.codeSystem,
                        code: this.state.code
                      }
                    ]
                  },
                  subject: {
                    reference: "Patient/pat1234"
                  },
                  authoredOn: "2018-08-08",
                  insurance: [{
                    reference: "Coverage/cov1234"
                  }],
                  performer: {
                    reference: "PractitionerRole/prarol1234"
                  }
                }
              },
              {
                resource: {
                  resourceType: "Patient",
                  id: "pat1234",
                  gender: this.state.gender,
                  birthDate: birthYear + "-12-23",
                  address: [
                    {
                      use: "home",
                      type: "both",
                      state: this.state.patientState
                    }
                  ]
                }
              },
              {
                resource: {
                  resourceType: "Coverage",
                  id: "cov1234",
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
                      reference: "Organization/org1234"
                    }
                  ]
                }
              },
              {
                resource: {
                  resourceType: "Location",
                  id: "loc1234",
                  address: {
                    line: [
                      "100 Good St"
                    ],
                    city: "Bedford",
                    state: this.state.practitionerState,
                    postalCode: "01730"
                  }
                }
              },
              {
                resource: {
                  resourceType: "PractitionerRole",
                  id: "prarol1234",
                  practitioner: {
                    reference: "Practitioner/pra1234"
                  },
                  location: [
                    {
                      reference: "Location/loc1234"
                    }
                  ]
                }
              },
              {
                resource: {
                  resourceType: "Organization",
                  id: "org1234",
                  name: "Centers for Medicare and Medicaid Services"
                }
              },
              {
                resource: {
                  resourceType: "Practitioner",
                  id: "pra1234",
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
        };
      }
      return request;
    }
}



