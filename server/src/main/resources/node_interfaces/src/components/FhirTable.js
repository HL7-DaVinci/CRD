import React, {Component} from 'react';
import FhirTableRow from './FhirTableRow';
import './DataTable.css';
import {getBaseUrl} from '../Utils';

let baseUrl = getBaseUrl();

export default class FhirTable extends Component {
    constructor(props){
        super(props);
        this.state={
            resources: {}
        };
    }

    componentDidMount(){
        document.body.style.backgroundColor = "white"// Set the style
              const data = fetch(baseUrl + 'api/fhir', {

            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
            }).then(response=>{
                return response.json();
            }).then(json=>{
                console.log(json);
                this.setState({resources:json});
            })
            .catch(error=>{
                console.log(error);
                console.log("Couldn't load fhir data, make sure the server is running.")
            });
        console.log(data);
    }

    render() {
        return (
            <div className="fhirTable">
                <div>
                    <table className="table-responsive table-striped table">
                        <thead>
                        <tr>
                            <td>ID</td>
                            <td>Resource Type</td>
                            <td>Topic</td>
                            <td>Fhir Version</td>
                            <td>Filename</td>
                            <td>URL</td>
                            <td>Download</td>
                        </tr>
                        </thead>
                        <tbody>
                        {(this.state.resources instanceof Array)?this.state.resources.map((resource)=>{
                            return(<FhirTableRow resource = {resource} key = {resource.id} home = {this.props.home}></FhirTableRow>)
                        //    //return(<TableRow data = {rule} handleDeleteCB = {this.handleDeleteCB} key = {rule.id} edit={rule.id===this.state.editId} home={this.props.home}></TableRow>)
                        }):null}
                        </tbody>
                    </table>
                </div>
            </div>
        )
    }
}