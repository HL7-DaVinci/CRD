import React, {Component} from 'react';
import TableRow from './TableRow';
import './DataTable.css';
import {getBaseUrl} from '../Utils';

let baseUrl = getBaseUrl();

export default class DataTable extends Component {
    constructor(props){
        super(props);
        this.state={
            rules: {}
        };
    }

    componentDidMount(){
        document.body.style.backgroundColor = "white"// Set the style
              const data = fetch(baseUrl + 'api/data', {

            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
            }).then(response=>{
                return response.json();
            }).then(json=>{
                console.log(json);
                this.setState({rules:json});
            })
            .catch(error=>{
                console.log(error);
                console.log("Couldn't load data, make sure the server is running.")
            });
        console.log(data);
    }

    render() {
        return (
            <div className="dataTable">
                <form action="/reload" method="post">
                    <div class="spacer"></div>
                    <span class="reload">
                        <input name="vsac_api_key" type="password" placeholder="VSAC API KEY"/>
                        <input type="submit" value="Reload Data"/>
                    </span>
                </form>
                <div>
                    <table className="table-responsive table-striped table">
                        <thead>
                        <tr>
                            <td>Topic</td>
                            <td>Payor</td>
                            <td>Relevant Code</td>
                            <td>Code System</td>
                            <td>FHIR Version</td>
                            <td>Rule</td>
                        </tr>
                        </thead>
                        <tbody>
                        {(this.state.rules instanceof Array)?this.state.rules.map((rule)=>{
                            return(<TableRow data = {rule} key = {rule.id} home = {this.props.home}></TableRow>)
                            //return(<TableRow data = {rule} handleDeleteCB = {this.handleDeleteCB} key = {rule.id} edit={rule.id===this.state.editId} home={this.props.home}></TableRow>)
                        }):null}
                        </tbody>
                    </table>
                </div>
            </div>
        )
    }
}