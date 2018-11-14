import React, { Component } from "react";

/* Import Components */
import CheckBox from "../components/CheckBox";
import Input from "../components/Input";
import TextArea from "../components/TextArea";
import Select from "../components/Select";
import Button from "../components/Button";

class FormContainer extends Component {
  constructor(props) {
    super(props);

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
    //this.handleSelect = this.handleSelect.bind(this);
  }

  /* This lifecycle hook gets executed when the component mounts */


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
        
        <Button
          action={this.handleRuleDelete}
          type={"secondary"}
          title={"Delete"}
          style={buttonStyle}
        />{" "}

		<Button
          action={this.handleRuleUpdate}
          handleChange={this.state.newRule}
          type={"secondary"}
          title={"Edit"}
          style={buttonStyle}
        />{" "}
      </form>
    );
  }
}

//Trying to add row components to edit and delete individually
class Row extends Component{
	constructor(props){
		super(props);
	}
	
	render(){
		return (
			<tr>
				<td>{this.props.data.id}</td>
				
				<td>{this.props.data.equipmentCode}</td>
				
				<td>{this.props.data.codeSystem}</td>
				
				<td>{this.props.data.ageRangeLow}</td>
				
				<td>{this.props.data.ageRangeHigh}</td>
				
				<td>{this.props.data.gender}</td>
				
				<td>{this.props.data.infoLink}</td>
				
				<td>{this.props.data.noAuthNeeded}</td>
				
				<td className="glyphicon glyphicon-trash" onClick={() => {this.props.handleRuleDelete(this.props.row)}}></td>
				<td className="glyphicon glyphicon-edit" onClick={() => {this.props.handleRuleEdit(this.props.row)}} ></td>
			</tr>
			
		);
	}
}

const buttonStyle = {
  margin: "10px 10px 10px 10px"
};

export default FormContainer;