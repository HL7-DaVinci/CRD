import React, {Component} from 'react';
import TableRow from './TableRow';
import './DataTable.css';
import {getBaseUrl} from '../Utils';

let baseUrl = getBaseUrl();

export default class DataTable extends Component {
    constructor(props){
        super(props);
        this.state={
            rules: {},
            editId: null,
        };
    this.handleDeleteCB = this.handleDeleteCB.bind(this);
    this.buttonStuff = this.buttonStuff.bind(this);
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

    handleDeleteCB(id){
        console.log(id);
        this.setState({rules: this.state.rules.filter(function(rule) {
            return rule.id !== id;
        })});
    }

    buttonStuff(){
        const newData = this.state.rules;
        // TODO: the app should really be getting
        // the structure of the rule from somewhere.
        // There are currently multiple places the rule structure
        // is hard coded.  Adding a new rule will prove very cumbersome
        // without some refactoring and centralizing.
        const newRule = {'id':null,
        'ageRangeLow':null,
        'ageRangeHigh':null,
        'genderCode':null,
        'equipmentCode':"",
        'codeSystem':'',
        'patientAddressState':"",
        'providerAddressState':"",
        'noAuthNeeded': "",
        'infoLink':""
    }

    const newId = fetch(baseUrl + "api/data/", {
        method: 'POST',
        body: JSON.stringify(newRule),
        headers: {'Content-Type':'application/json'}
            }).then(function(response) {
                return response.text();
            }).then((json)=>{
                return json
            }).catch(function(err) {
                console.log(err)
            });

        newId.then(result=>{
            newRule.id = result;
            this.setState({editId:result});
            newData.unshift(newRule);
            this.setState({rules: newData});
        });
       
    }
     render() {
         return (
             <div className="dataTable">
               
                <div>
                    <table className="table-responsive table-striped table">
                        <thead>
                        <tr>
                            <td>Payor</td>
                            <td>Relevant Code</td>
                            <td>Code System URL</td>
                            <td>CQL</td>
                            <td>Edit</td>
                        </tr>
                        </thead>
                        <tbody>
                        {(this.state.rules instanceof Array)?this.state.rules.map((rule)=>{
                            return(<TableRow data = {rule} handleDeleteCB = {this.handleDeleteCB} key = {rule.id} edit={rule.id===this.state.editId} home={this.props.home}></TableRow>)
                        }):null}
                        </tbody>

                    </table>
                </div>
             </div>

         )

      }

}