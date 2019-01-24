import React, {Component} from 'react';
import TableRow from './TableRow';
import './DataTable.css';

let baseUrl = document.querySelector("meta[name='ctx']").getAttribute("content");
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
             {!this.props.home?<div className="button-wrapper">
                    <button className="add-button" onClick={()=>{this.buttonStuff()}}><span className="glyphicon glyphicon-plus-sign"></span></button>
                </div>:null}
               
                <div>
                    <table className="table-responsive table-striped table">
                        <thead>
                        <tr>
                            {/* <td>ID</td> */}
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
                            return(<TableRow data = {rule} handleDeleteCB = {this.handleDeleteCB} key = {rule.id} edit={rule.id===this.state.editId} home={this.props.home}></TableRow>)
                        }):null}
                        </tbody>

                    </table>
                </div>
             </div>

         )

      }

}