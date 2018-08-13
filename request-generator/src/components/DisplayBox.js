import React, {Component} from 'react';
import { connect } from 'react-redux';

class DisplayBox extends Component{
    constructor(props){
        super(props);
        this.state={
            value: ""
        };
    }

    render() {
        console.log(this.props.response);
        return (
            <div>
                {this.props.response}
            </div>
        )
    }
} 


function mapStateToProps({value}){
    return {value};
}
export default connect(mapStateToProps)(DisplayBox);