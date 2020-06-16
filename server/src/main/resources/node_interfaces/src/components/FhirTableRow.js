import React, {Component} from 'react';
import {codeSystemConversion} from './ComponentConstants';
import Select from './Select';
import './DataTable.css';


// receives resource as a prop
export default class FhirTableRow extends Component {
    constructor(props){
        super(props);
        this.state={
            resource: {}
        };
    }

    componentDidMount() {
        const resource = this.props.resource;
        Object.keys(resource).forEach(key => {
            // replace null values with a '-'
            if(resource[key] == null) {
                resource[key] = "-";
            }
        });

        this.setState({resource});
    }

    render() {
        return(
            <tr>
                <td>{this.state.resource.id}</td>
                <td>{this.state.resource.resourceType}</td>
                <td>{this.state.resource.readableTopic}</td>
                <td>{this.state.resource.fhirVersion}</td>
                <td>{this.state.resource.filename}</td>
                <td>{this.state.resource.url}</td>
                <td><a href={this.state.resource.link}>download</a></td>
            </tr>
        )
    }
} 