import React, {Component} from 'react';
import Form from "../components/Form";
import TableRow from './TableRow';
import './DataTable.css';
export default class DataTable extends Component {
    constructor(props){
        super(props);
        this.state={
            rules: {}
        };
    this.handleDeleteCB = this.handleDeleteCB.bind(this);
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
                console.log(json);
                this.setState({rules:json});
            })
            .catch(error=>{
                console.log(error);
                console.log("Couldn't load data, make sure the server is running.")
            });
        console.log(data);

    }

    handleDeleteCB(id){
        console.log(id);
        this.setState({rules: this.state.rules.filter(function(rule) {
            return rule.id !== id;
        })});
    }
     render() {
         return (
             <div className="dataTable">
                <div>
                </div>
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
                            return(<TableRow data = {rule} handleDeleteCB = {this.handleDeleteCB} key = {rule.id}></TableRow>)
                        }):null}
                        </tbody>

                    </table>
                </div>
             </div>

         )

      }

}