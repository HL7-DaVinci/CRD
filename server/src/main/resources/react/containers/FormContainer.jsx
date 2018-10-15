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
        genderCode: [],
        infoLink: "",
        noAuthNeeded: ""
      },

      genderOptions: ["Male", "Female", "Other"],
      authNeededOptions: ["True", "False"]
    };
    this.handleTextArea = this.handleTextArea.bind(this);
    this.handleFormSubmit = this.handleFormSubmit.bind(this);
    this.handleClearForm = this.handleClearForm.bind(this);
    this.handleCheckBox = this.handleCheckBox.bind(this);
    this.handleInput = this.handleInput.bind(this);
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
    console.log("Inside handleTextArea");
    let value = e.target.value;
    this.setState(
      prevState => ({
        newRule: {
          ...prevState.newRule,
          about: value
        }
      }),
      () => console.log(this.state.newRule)
    );
  }

  handleCheckBox(e) {
    const newSelection = e.target.value;
    let newSelectionArray;

    if (this.state.newRule.gender.indexOf(newSelection) > -1) {
      newSelectionArray = this.state.newRule.gender.filter(
        s => s !== newSelection
      );
    } else {
      newSelectionArray = [...this.state.newRule.gender, newSelection];
    }

    this.setState(prevState => ({
      newRule: { ...prevState.newRule, gender: newSelectionArray }
    }));
  }

  //posting data to the rule's database upon Submit
  handleFormSubmit(e) {
    e.preventDefault();
    let ruleData = this.state.newUser;

    fetch("http://localhost:8090/api/data", {
      method: "POST",
      body: JSON.stringify(ruleData),
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json"
      }
    }).then(response => {
      response.json().then(data => {
        console.log("Successful" + data);
      });
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
          genderCode: [],
          infoLink: "",//how to add link?,
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
              name={"ageLow"}
              title={"Age Range Low"}
              value={this.state.newRule.ageLow}
              placeholder={"Enter the low age range boundary"}
              handleChange={this.handleInput}
          />{" "}

          <Input
              inputType={"number"}
              name={"ageHigh"}
              title={"Age Range High"}
              value={this.state.newRule.age}
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
          selectedOptions={this.state.newRule.authNeeded}
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

      </form>
    );
  }
}

const buttonStyle = {
  margin: "10px 10px 10px 10px"
};

export default FormContainer;