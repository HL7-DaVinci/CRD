import React, {Component} from 'react';
import './index.css';

let componentConstants = require('./ComponentConstants');

export default class DataTable extends Component {
    constructor(props){
        super(props);
        this.state={
            rules: {}
        };
         
    }

    componentDidMount(){

        document.body.style.backgroundColor = "white"// Set the style

        const data = fetch('http://localhost:8090/api/data', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
            }).then(response=>{
                return response.json();
            }).then(json=>{
                this.setState({rules:json});
            })
            .catch(error=>{
                console.log("Couldn't load data, make sure the server is running.")
            });
        
    }
     render() {
         return (
             <div className="dataTable">
                 {console.log(this.state.rules)}
                <div>
                    <table className="table-responsive table-striped table">
                        <thead>
                        <tr>
                            <td>ID</td>
                            <td>Rule Applies at Age</td>
                            <td>Rule Ends at Age</td>
                            <td>Gender</td>
                            <td>Relevant Code (CPT / HCPCS)</td>
                            <td>Code System URL</td>
                            <td>Patient Address</td>
                            <td>Practitioner Address</td>
                            <td>Documentation Required</td>
                            <td>Coverage Documentation Requirement Information Link</td>
                        </tr>
                        </thead>
                        <tbody>
                        {(this.state.rules instanceof Array)?this.state.rules.map((rule)=>{
                            return(<tr>
                                <td >{rule.id}</td>
                                <td >{rule.ageRangeLow}</td>
                                <td >{rule.ageRangeHigh}</td>
                                <td >{rule.genderCode}</td>
                                <td >{rule.equipmentCode}</td>
                                <td title={rule.codeSystem}>
                                  <span>{componentConstants.codeSystemConversion[rule.codeSystem]}</span>
                                </td>
                                <td >{rule.patientAddressState}</td>
                                <td >{rule.providerAddressState}</td>
                                <td >{rule.noAuthNeeded?"false":"true"}</td>
                                <td >{rule.infoLink}</td>
                            </tr>)
                        }):null}
                        </tbody>

                    </table>

                </div>
             </div>

         )

    }
} 

