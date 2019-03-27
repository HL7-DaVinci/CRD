import React, {Component} from 'react';
import {codeSystemConversion} from './ComponentConstants';
import Select from './Select';
import './DataTable.css';

const genderCodeOptions=[
    {key:null,value:"",text:""},
    {key:"M",value:"M",text:"male"},
    {key:"F",value:"F",text:"female"}
];

const trueFalse=[
    {key:true,value:true,text:"true"},
    {key:false,value:false,text:"false"}
]
const stateOptions = [
    { key: null, value: "", text: ""},
    { key: 'AL', value: 'AL', text: 'Alabama' },
    { key: 'AK', value: 'AK', text: 'Alaska' },
    { key: 'AZ', value: 'AZ', text: 'Arizona' },
    { key: 'AR', value: 'AR', text: 'Arkansas' },
    { key: 'CA', value: 'CA', text: 'California' },
    { key: 'CO', value: 'CO', text: 'Colorado' },
    { key: 'CT', value: 'CT', text: 'Connecticut' },
    { key: 'DE', value: 'DE', text: 'Delaware' },
    { key: 'DC', value: 'DC', text: 'District Of Columbia' },
    { key: 'FL', value: 'FL', text: 'Florida' },
    { key: 'GA', value: 'GA', text: 'Georgia' },
    { key: 'HI', value: 'HI', text: 'Hawaii' },
    { key: 'ID', value: 'ID', text: 'Idaho' },
    { key: 'IL', value: 'IL', text: 'Illinois' },
    { key: 'IN', value: 'IN', text: 'Indiana' },
    { key: 'IA', value: 'IA', text: 'Iowa' },
    { key: 'KS', value: 'KS', text: 'Kansas' },
    { key: 'KY', value: 'KY', text: 'Kentucky' },
    { key: 'LA', value: 'LA', text: 'Louisiana' },
    { key: 'ME', value: 'ME', text: 'Maine' },
    { key: 'MD', value: 'MD', text: 'Maryland' },
    { key: 'MA', value: 'MA', text: 'Massachusetts' },
    { key: 'MI', value: 'MI', text: 'Michigan' },
    { key: 'MN', value: 'MN', text: 'Minnesota' },
    { key: 'MS', value: 'MS', text: 'Mississippi' },
    { key: 'MO', value: 'MO', text: 'Missouri' },
    { key: 'MT', value: 'MT', text: 'Montana' },
    { key: 'NE', value: 'NE', text: 'Nebraska' },
    { key: 'NV', value: 'NV', text: 'Nevada' },
    { key: 'NH', value: 'NH', text: 'New Hampshire' },
    { key: 'NJ', value: 'NJ', text: 'New Jersey' },
    { key: 'NM', value: 'NM', text: 'New Mexico' },
    { key: 'NY', value: 'NY', text: 'New York' },
    { key: 'NC', value: 'NC', text: 'North Carolina' },
    { key: 'ND', value: 'ND', text: 'North Dakota' },
    { key: 'OH', value: 'OH', text: 'Ohio' },
    { key: 'OK', value: 'OK', text: 'Oklahoma' },
    { key: 'OR', value: 'OR', text: 'Oregon' },
    { key: 'PA', value: 'PA', text: 'Pennsylvania' },
    { key: 'RI', value: 'RI', text: 'Rhode Island' },
    { key: 'SC', value: 'SC', text: 'South Carolina' },
    { key: 'SD', value: 'SD', text: 'South Dakota' },
    { key: 'TN', value: 'TN', text: 'Tennessee' },
    { key: 'TX', value: 'TX', text: 'Texas' },
    { key: 'UT', value: 'UT', text: 'Utah' },
    { key: 'VT', value: 'VT', text: 'Vermont' },
    { key: 'VA', value: 'VA', text: 'Virginia' },
    { key: 'WA', value: 'WA', text: 'Washington' },
    { key: 'WV', value: 'WV', text: 'West Virginia' },
    { key: 'WI', value: 'WI', text: 'Wisconsin' },
    { key: 'WY', value: 'WY', text: 'Wyoming' },
]
// recieves data as a prop
export default class TableRow extends Component {
    constructor(props){
        super(props);
        this.state={
            data: {},
            edit: this.props.edit,
            deleteConfirm: false
        };
        this.handleRuleDeletePre = this.handleRuleDeletePre.bind(this);
        this.handleRuleDelete = this.handleRuleDelete.bind(this);
        this.handleRuleEdit = this.handleRuleEdit.bind(this);
        this.handleUpdate = this.handleUpdate.bind(this);
        this.handleEnter = this.handleEnter.bind(this);
        
    }

    componentDidMount() {
        const data = this.props.data;
        Object.keys(data).forEach(key => {
            if(data[key] == null) {
                data[key] = "-";
            }
        });

        this.setState({data});
    }

    handleRuleDelete() {
        // delete the rule from the external table database
        fetch("/api/data/"+this.state.data.id, {
        method: 'DELETE',
            }).then(function(response) {
                // delete the rule from the internal table representation
                return response.json();
            }).then((json)=>{
                if(json >=0){
                    this.props.handleDeleteCB(this.state.data.id);
                }
            }).catch(function(err) {
                console.log(err)
            });
    }

    handleRuleDeletePre() {
        this.setState({deleteConfirm:!this.state.deleteConfirm})
    }
    handleRuleEdit() {
        console.log(this.state.data);
        if(this.state.edit) {
            fetch("/api/data/"+this.state.data.id, {
                method: 'PUT',
                body: JSON.stringify(this.state.data),
                headers: {'Content-Type':'application/json'}
                    }).then(function(response) {
                        console.log(response.body);
                    }).catch(function(err) {
                        console.log(err)
                    });
        }
        this.setState({edit:!this.state.edit})
    }

    handleUpdate(e,value){
        const updatedData = this.state.data;
        updatedData[value] = e.target.value;
        this.setState({data:updatedData});
    }

    handleEnter(e){
        if(e.key==='Enter'){
            this.handleRuleEdit()
        }
    }

     render() {
         return(

                <tr> 
                    <td >{this.state.edit?<input onKeyPress={this.handleEnter} onChange={(e)=>{this.handleUpdate(e,"infoLink")}} className="informationInput formInput"placeholder={this.state.data.infoLink}></input>:this.state.data.payor}</td>
                    <td >{this.state.edit?<input onKeyPress={this.handleEnter} onChange={(e)=>{this.handleUpdate(e,"equipmentCode")}} className="codeInput formInput" placeholder={this.state.data.equipmentCode}></input>:this.state.data.code}</td>
                    <td title={this.state.data.codeSystem}>{this.state.edit?<input onKeyPress={this.handleEnter} onChange={(e)=>{this.handleUpdate(e,"codeSystem")}} className="formInput" placeholder={this.state.data.codeSystem} ></input>:<span>{codeSystemConversion[this.state.data.codeSystem]}</span>}</td>
                    <td><a href={this.state.data.link}>download</a></td>
                    <td><a href={this.state.data.editLink}>edit</a></td>
                </tr>

         )
    }
} 