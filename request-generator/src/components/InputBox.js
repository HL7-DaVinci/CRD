import React, {Component} from 'react';
import { connect } from 'react-redux';


class InputBox extends Component {
    constructor(props){
        super(props);
        this.state={
            value: ""
        };

    this.onInputChange = this.onInputChange.bind(this);

    }
    onInputChange(event){
        this.setState({ value: event.target.value });
        this.props.updateCB(this.props.elementName, event.target.value);

    }
    render() {
        return (
            <div>
            <input
            className={"form-control input-text " + this.props.extraClass}
            name={this.props.elementName}
            value={this.state.value}
            onChange={this.onInputChange}

            ></input><span className="floating-label">{this.props.elementName}</span>
            </div>
        )
    }
}

function mapStateToProps({value}){
    return {value};
}
export default connect(mapStateToProps)(InputBox);