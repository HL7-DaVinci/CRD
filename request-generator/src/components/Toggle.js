import React, {Component} from 'react';
import { connect } from 'react-redux';


export default class Toggle extends Component {
    constructor(props){
        super(props);
        this.state={
            value: "",
            option1class: "genderBlockMaleUnselected",
            option2class: "genderBlockFemaleUnselected"
        };

    this.clickedOption1 = this.clickedOption1.bind(this);
    this.clickedOption2 = this.clickedOption2.bind(this);
    }


    clickedOption1(){
        if(this.state.option1class==="genderBlockMaleUnselected"){
            this.setState({option1class : "genderBlockMaleSelected"})
            this.props.updateCB(this.props.elementName, this.props.options.option1.value);

            if(this.state.option2class==="genderBlockFemaleSelected"){
                this.setState({option2class : "genderBlockFemaleUnselected"})
            }
        }else{
            this.setState({option1class: "genderBlockMaleUnselected"})
            this.props.updateCB(this.props.elementName, "");
        }

    }

    clickedOption2(){
        if(this.state.option2class==="genderBlockFemaleUnselected"){
            this.setState({option2class : "genderBlockFemaleSelected"})
            this.props.updateCB(this.props.elementName, this.props.options.option2.value);
            if(this.state.option1class==="genderBlockMaleSelected"){
                this.setState({option1class : "genderBlockMaleUnselected"})
            }
        }else{
            this.setState({option2class: "genderBlockFemaleUnselected"})
            this.props.updateCB(this.props.elementName, "");
        }

    }
    render() {
        return (
            <div>
            <div
            name={this.props.elementName}
            >
            <button onClick={this.clickedOption1} className={this.state.option1class+" genderBlockMale btn btn-class"}>{this.props.options.option1.text}</button>
            <button onClick={this.clickedOption2} className={this.state.option2class+" genderBlockFemale btn btn-class"}>{this.props.options.option2.text}</button>
            </div>
            </div>
        )

    }
}
