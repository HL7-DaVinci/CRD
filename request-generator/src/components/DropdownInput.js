import React, {Component} from 'react';
import { connect } from 'react-redux';
import {Dropdown} from 'semantic-ui-react';

const options = [
    { key: 'CPAP', text: '94660', value: 'fe' },
    { key: 'Wheelchair', text: '97542', value: '97542' },
    { key: 'Crutches', text: 'E0110', value: 'E0110' },
    { key: 'Hospital Bed', text: 'E0250', value: 'E0250' },
    { key: 'Continuous Glucose Monitoring', text: '95250', value: '95250' },
    { key: 'Nebulizer', text: '94640', value:'94640' },
    { key: 'Glucose Test Strip', text:'82947', value:'82947'},
  ]
  
class DropdownInput extends Component {
    constructor(props){
        super(props);
        this.state = { options }
        };


  handleAddition = (e, { value }) => {
    this.setState({
      options: [{ text: value, value }, ...this.state.options],
    })
  }

  handleChange = (e, { value }) => {
    this.props.updateCB(this.props.elementName, value)
    this.setState({ currentValue: value })
  }

  render() {
    const { currentValue } = this.state

    return (
      <Dropdown
      className="dropdownCode"
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


function mapStateToProps({value}){
    return {value};
}
export default connect(mapStateToProps)(DropdownInput);