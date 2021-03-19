import { getBaseUrl } from '../../Utils';
import React, { Component } from 'react';
import {getUrlParameter, makeUrlParams} from './url';
import {grant_type} from './constants';
import { Dropdown } from 'semantic-ui-react'

export default class Index extends Component {
    constructor(props) {
        super(props);
        this.state = {
            token: '',
            params: {},
            patients: []
        };
    }

    componentDidMount() {
        const state = getUrlParameter("state");
        const code = getUrlParameter("code");
        const { tokenUri, client_id, serviceUri, redirect_uri } = JSON.parse(sessionStorage[state]);
        const data = {code, grant_type, redirect_uri, client_id};
        const body = makeUrlParams(data);
        this.makeTokenRequest(tokenUri, body, serviceUri);
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
                {this.state.patients ? <div>
                    {this.renderPatients(this.state.patients)}
                </div> : 
                <div>not loaded</div>}
            </div>
        )
    }
}
