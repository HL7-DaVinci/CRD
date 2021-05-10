import React, { Component } from 'react';
import {getUrlParameter} from './url';
import { getBaseUrl, getBackend } from '../../Utils';
import { Dropdown } from 'semantic-ui-react'

import './launch.css';

export default class Launcher extends Component {
    constructor(props) {
        super(props);
        this.state = {
            serviceUri: '',
            clientId: '',
            clients: [],
        };
        this.baseUrl = getBaseUrl();
        this.launch = this.launch.bind(this);
        this.save = this.save.bind(this);
        this.fetchClients = this.fetchClients.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.handleInput = this.handleInput.bind(this);
    }

    componentDidMount() {
        fetch(`${this.baseUrl}api/clients`, {method: "GET"}).then(response => response.json()).then((data) => {
            const serviceUri = getUrlParameter("iss");
            this.setState({clients: data});
            console.log(data);
            if (serviceUri) {
                // provided iss
                const client = data.find(d => d.iss === serviceUri);
                if(client) {
                    // auto-launch
                    this.launch(client.iss, client.client_id);
                } else {
                    this.setState({serviceUri});
                }
            }
        })


    }

    fetchClients() {
        const baseUrl = getBackend();
        return fetch(`${baseUrl}api/clients`, {method: "GET"}).then(response => response.json()).then(data=>this.setState({clients: data}));
    }
    
    save(serviceUri, clientId) {
        fetch(`${this.baseUrl}api/clients`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                iss: serviceUri,
                client_id: clientId
            })
        })
    }

    launch(serviceUri, clientId) {
        // const serviceUri = client.iss;
        // const clientId = client.client_id;
        localStorage.setItem("lastAccessedServiceUri", serviceUri);
        const scope = ["launch", "user/Observation.read", "user/Patient.read", "patient/Observation.read", "patient/Patient.read", "patient/Coverage.read", "patient/Condition.read", "user/Practitioner.read"].join(" ");
        const state = Math.round(Math.random() * 100000000).toString();
        const launchUri = window.location.protocol + "//" + window.location.host + window.location.pathname;
        const redirectUri = launchUri.replace("launch", "index");

        var conformanceUri = serviceUri + "/metadata?_format=json";
        let conformanceStatement;
        const conformanceGet = new XMLHttpRequest();
        conformanceGet.open("GET", conformanceUri);
        conformanceGet.setRequestHeader("Content-Type", "application/json");
        conformanceGet.setRequestHeader("Accept", "application/json");

        conformanceGet.onload = function () {
            if (conformanceGet.status === 200) {
                try {

                    conformanceStatement = JSON.parse(conformanceGet.responseText);
                } catch (e) {
                    const error = "Unable to parse conformance statement.";
                    document.body.innerText = error;
                    return;
                }
                redirect(conformanceStatement);
            } else {
                const error = "Conformance statement request failed. Returned status: " + conformanceGet.status;
                document.body.innerText = error;
                return;
            }
        };
        conformanceGet.send();

        function redirect(conformanceStatement) {
            var authUri, tokenUri;
            var smartExtension = conformanceStatement.rest[0].security.extension.filter(function(e) {
                return e.url === "http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris";
            });
        
            smartExtension[0].extension.forEach(function(arg) {
                if (arg.url === "authorize") {
                authUri = arg.valueUri;
                } else if (arg.url === "token") {
                tokenUri = arg.valueUri;
                }
            });
        
            // retain a couple parameters in the session for later use
            sessionStorage[state] = JSON.stringify({
                client_id: clientId, // this is how the key should be formatted
                serviceUri: serviceUri,
                redirect_uri: redirectUri,
                tokenUri: tokenUri,
            });
        
            // finally, redirect the browser to the authorizatin server and pass the needed
            // parameters for the authorization request in the URL
            let authRedirect =         
                authUri +
                "?" +
                "response_type=code&" +
                "client_id=" +
                encodeURIComponent(clientId) +
                "&" +
                "scope=" +
                encodeURIComponent(scope) +
                "&" +
                "redirect_uri=" +
                encodeURIComponent(redirectUri) +
                "&" +
                "aud=" +
                encodeURIComponent(serviceUri) +
                "&" +
                "state=" +
                state;
            
            console.log(authRedirect);
            window.location.href = authRedirect;
            }
    }


    handleChange(e, { value }){
        const client = JSON.parse(value);
        this.setState({ serviceUri: client.iss, clientId: client.client_id });
    } 

    handleInput(e, name) {
        this.setState({[name]: e.target.value});
    }
    renderClients() {
        const clients = this.state.clients;
        const options = clients.map((client) =>{
            return {
                key: client.client_id,
                value: JSON.stringify(client),
                text: `${client.iss} - ${client.client_id}`
            }
        })

        const currentClient = {
            iss: this.state.serviceUri,
            client_id: this.state.clientId
        }
        return (<div className = "clientDropdown">
            <div>Saved Clients: </div>
            <Dropdown 
                placeholder='Select client'
                fluid
                search
                selection
                onChange = {this.handleChange}
                value = {JSON.stringify(currentClient)}
                options={options}
            />
        </div>)
    }

    render() {

        return <div> 
            {this.renderClients()}
            <div>
                ISS: 
                <input 
                className="client-input" 
                value={this.state.serviceUri} 
                onChange={(e)=> {this.handleInput(e,"serviceUri")}}/>
            </div>
            <div>
                Client Id: 
                <input className="client-input" 
                value={this.state.clientId} 
                onChange={(e)=> {this.handleInput(e,"clientId")}}/>
            </div>
            <button 
            variant="Primary" 
            onClick = {() => {this.launch(this.state.serviceUri, this.state.clientId)}}>
                Launch!
            </button>
            <button 
            variant="Primary" 
            onClick = {() => {this.save(this.state.serviceUri, this.state.clientId)}}>
                Save Client
            </button>
        </div>
    }
}