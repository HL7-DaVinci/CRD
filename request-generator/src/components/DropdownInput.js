import React, {Component} from 'react';
import {Dropdown} from 'semantic-ui-react';

const defaultValues = [
    { key: 'CPAP', text: '94660', value: '94660', codeSystem: 'http://www.ama-assn.org/go/cpt' },
    { key: 'Wheelchair', text: '97542', value: '97542', codeSystem: 'http://www.ama-assn.org/go/cpt' },
    { key: 'Crutches', text: 'E0110', value: 'E0110', codeSystem: 'https://bluebutton.cms.gov/resources/codesystem/hcpcs' },
    { key: 'Hospital Bed', text: 'E0250', value: 'E0250', codeSystem: 'https://bluebutton.cms.gov/resources/codesystem/hcpcs' },
    { key: 'Continuous Glucose Monitoring', text: '95250', value: '95250', codeSystem: 'http://www.ama-assn.org/go/cpt' },
    { key: 'Nebulizer', text: '94640', value:'94640', codeSystem: 'http://www.ama-assn.org/go/cpt' },
    { key: 'Glucose Test Strip', text:'82947', value:'82947', codeSystem: 'http://www.ama-assn.org/go/cpt'},
    { key: 'Oxygen Thing', text: 'E0424', value:'E0424', codeSystem:'https://bluebutton.cms.gov/resources/codesystem/hcpcs' }
]

function dropDownOptions() {
  return defaultValues.map((v) => {return {key: v.key, text: `${v.key} - ${v.value}`, value: v.value}})
}

let blackBorder = "blackBorder";

export default class DropdownInput extends Component {
  constructor(props){
    super(props);
    this.state = { options: dropDownOptions() }
  };

  handleAddition = (e, { value }) => {
    this.setState({
      options: [{ text: value, value }, ...this.state.options],
    })
  }

  handleChange = (e, { value }) => {
    this.props.updateCB(this.props.elementName, value)
    this.props.updateCB('codeSystem', defaultValues.find((v) => v.value === value).codeSystem)
    this.setState({ currentValue: value })
  }


  render() {
    const { currentValue } = this.state
    if(currentValue){
        blackBorder = "blackBorder";
    }else{
        blackBorder = "";
    }
    return (
      <Dropdown
      className={"dropdownCode " +blackBorder}
        options={this.state.options}
        placeholder='Choose Code'
        search
        selection
        fluid
        allowAdditions
        value={currentValue}
        onAddItem={this.handleAddition}
        onChange={this.handleChange}
      />
    )
  }
}