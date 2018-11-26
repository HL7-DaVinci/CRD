import React, {Component} from 'react';
import CheckBox from "../components/CheckBox";
import Input from "../components/Input";
import TextArea from "../components/TextArea";
import Select from "../components/Select";
import Button from "../components/Button";
import Form from "../components/Form";

import './index.css';
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

           //posting data to the rule's database upon Submit
          handleFormSubmit(e) {
            e.preventDefault();
            let ruleData = this.state.newRule;

            fetch("http://localhost:8090/api/data", {
              method: "POST",
              body: JSON.stringify(ruleData),
              headers: {
                Accept: "application/json",
                "Content-Type": "application/json"
              }
            }).then(response => {
              response.json().then(rule => {
                console.log("Successful" + rule);
              });
            });
          }

           //Deleting rule from rule db
          handleRuleDelete(rule){
            var data = {
                id: rule.id
            }
            fetch('"http://localhost:8090/api/data/"+rule.id', {
                method: 'DELETE',
                body: JSON.stringify(rule)
            }).then(function(response) {
                if (response.status >= 400) {
                  throw new Error("Bad response from server");
                }
                return response.json();
            }).then(function(rule) {
                if(data === "success"){
                   this.setState({msg: "Rule has been deleted."});  
                }
            }).catch(function(err) {
                console.log(err)
            });
                }
            //allow updating of rule
            handleRuleUpdate() {
            const request = new Request('"http://localhost:8090/api/data/"+rule.id', {
              method: 'PUT',
              headers: new Headers({
                'Content-Type': 'application/json'
              }), 
              body: JSON.stringify()
            });
                }

                this.state = {
      newRule: {
        id: "",
        equipmentCode: "",
        codeSystem: "",
        ageRangeLow: "",
        ageRangeHigh: "",
        gender: [],
        infoLink: "",
        noAuthNeeded: ""
      },

      genderOptions: ["Male", "Female", "Other"],
      authNeededOptions: ["Yes", "No"]
    };
    this.handleTextArea = this.handleTextArea.bind(this);
    this.handleFormSubmit = this.handleFormSubmit.bind(this);
    this.handleClearForm = this.handleClearForm.bind(this);
    this.handleCheckBox = this.handleCheckBox.bind(this);
    this.handleInput = this.handleInput.bind(this);
    this.handleRuleDelete = this.handleRuleDelete.bind(this);
    this.handleRuleUpdate = this.handleRuleUpdate.bind(this);
    this.handleSelect = this.handleSelect.bind(this);
  }

 
  //Handling the various types of inputs and adding to a newRule
  handleInput(e) {
    let value = e.target.value;
    let name = e.target.name;
    this.setState(
      prevState => ({
        newRule: {
          ...prevState.newRule,
          [name]: value
        }
      }),
      () => console.log(this.state.newRule)
    );
  }

  //function to get text area inputs
  handleTextArea(e) {
    console.log("handleTextArea");
    let value = e.target.value;
    this.setState(prevState => ({
        newRule: {
          ...prevState.newRule, infoLink: value
        }
      }),
      () => console.log(this.state.newRule)
    );
  }

  //function to get check box inputs
  handleCheckBox(e) {
    const newSelection = e.target.value;
    let newSelectionArray;

    if (this.state.newRule.noAuthNeeded.indexOf(newSelection) > -1) {
      newSelectionArray = this.state.newRule.noAuthNeeded.filter(
        s => s !== newSelection
      );
    } else {
      newSelectionArray = [...this.state.newRule.noAuthNeeded, newSelection];
    }

    this.setState(prevState => ({
      newRule: { ...prevState.newRule, noAuthNeeded: newSelectionArray }
    }));
  }

  handleClearForm(e) {
    e.preventDefault();
    this.setState({
      newRule: {
          id: "",
          equipmentCode: "",
          codeSystem: "",
          ageRangeLow: "",
          ageRangeHigh: "",
          gender: [],
          infoLink: "",
          noAuthNeeded: ""
      }
    });
  }


  //Displaying the form fields and handling their inputs
  render() {
    return (
      <form className="container-fluid" onSubmit={this.handleFormSubmit}>
        <Input
          inputType={"text"}
          title={"Equipment Code"}
          name={"equipmentCode"}
          value={this.state.newRule.equipmentCode}
          placeholder={"Enter the equipment code"}
          handleChange={this.handleInput}
        />{" "}

        <Input
          inputType={"text"}
          name={"codeSystem"}
          title={"Code System"}
          value={this.state.newRule.codeSystem}
          placeholder={"Enter the Code System"}
          handleChange={this.handleInput}
        />{" "}

      <Input
          inputType={"number"}
          name={"ageRangeLow"}
          title={"Age Range Low"}
          value={this.state.newRule.ageRangeLow}
          placeholder={"Enter the low age range boundary"}
          handleChange={this.handleInput}
      />{" "}

      <Input
          inputType={"number"}
          name={"ageRangeHigh"}
          title={"Age Range High"}
          value={this.state.newRule.ageRangeHigh}
          placeholder={"Enter the high age range boundary"}
          handleChange={this.handleInput}
      />{" "}
      
        <Select
          title={"Gender"}
          name={"gender"}
          options={this.state.genderOptions}
          value={this.state.newRule.gender}
          placeholder={"Select Gender"}
          handleChange={this.handleInput}
        />{" "}

        <CheckBox
          title={"Authorization Required?"}
          name={"authNeeded"}
          options={this.state.authNeededOptions}
          selectedOptions={this.state.newRule.noAuthNeeded}
          handleChange={this.handleCheckBox}
        />{" "}

        <TextArea
          title={"Information Link"}
          rows={10}
          value={this.state.newRule.infoLink}
          name={"infoLink"}
          handleChange={this.handleTextArea}
          placeholder={"Enter link to further information"}
        />

        //Buttons for Submitting a rule and clearing the form -- edit and delete buttons are within the table code above

        <Button
          action={this.handleFormSubmit}
          type={"primary"}
          title={"Submit"}
          style={buttonStyle}
        />{" "}

        <Button
          action={this.handleClearForm}
          type={"secondary"}
          title={"Clear"}
          style={buttonStyle}
        />{" "}
      </form>

      const buttonStyle = {
  margin: "10px 10px 10px 10px"
};
        
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
                                <td >{rule.codeSystem}</td>
                                <td >{rule.patientAddressState}</td>
                                <td >{rule.providerAddressState}</td>
                                <td >{rule.noAuthNeeded?"false":"true"}</td>
                                <td >{rule.infoLink}</td>
                                <td className="glyphicon glyphicon-trash" onClick={() => {this.props.handleRuleDelete(this.props.row)}}></td>
                                <td className="glyphicon glyphicon-edit" onClick={() => {this.props.handleRuleEdit(this.props.row)}} ></td>
                            </tr>)
                        }):null}
                        </tbody>

                    </table>

                </div>
             </div>

         )
}