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

    submit_info(){
        const birthYear = 2018-parseInt(this.state.age,10);
        let json_request = {
            hookInstance: "d1577c69-dfbe-44ad-ba6d-3e05e953b2ea",
            fhirServer: "http://hooks.smarthealthit.org:9080",
            hook: "order-review",
            fhirAuthorization: {
              access_token: "some-opaque-fhir-access-token",
              token_type: "Bearer",
              expires_in: 300,
              scope: "patient/Patient.read patient/Observation.read",
              subject: "cds-service4"
            },
            user: "Practitioner/example",
            context: {
              patientId: "1288992",
              encounterId: "89284",
              orders: {
                resourceType: "Bundle",
                entry: [
                  {
                    resource: {
                      resourceType: "DeviceRequest",
                      note: [
                        {
                          text: "a sample device request"
                        }
                      ]
                    }
                  }
                ]
              }
            },
            prefetch: {
              patient: {
                resourceType: "Patient",
                gender: this.state.gender,
                birthDate: birthYear + "-12-23",
                id: "1288992",
                active: true
              }
            }
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

            const fhirResponse = await fetch("http://localhost:8080/cds-services/coverage-requirements-discovery",{
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(json_request)
            }).then(response => {
                return response.json();
            });
            console.log(fhirResponse);
            this.setState({response: fhirResponse});
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


    validateState(){
        const validationResult = {};
        Object.keys(this.validateMap).forEach(key => {
            if(this.state[key] && this.validateMap[key](this.state[key])){
                // We want to know if the result is not correct, so 1 here means 
                // true, or that the input failed.  Since 1 will return true and 0
                // false, a ternary operator is used later to choose the correct style
                // for the correct input box.
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

        // There's a little boolean logic being represented as integer
        // multiplication.  If there's any invalid inputs, we want to 
        // swap the submit buttons colors, but we don't want to treat
        // empty inputs as invalid ones.  


        const validationResult = this.validateState();
        const total = Object.keys(validationResult).reduce((previous,current) =>{
            return validationResult[current]*previous
        },1);

        return (
            <div>
            <div className="form-group container left-form">
                {Object.keys(this.state)
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
