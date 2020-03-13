import React, {Component} from 'react'
import { format } from 'date-fns'
import DetailEntry from './DetailEntry'
import './request.css'

export default class RequestEntry extends Component {
    constructor(props) {
        super(props);
        let timestamp = this.props.data.timestamp;
        if (!timestamp) {
            timestamp = new Date()
        }
        timestamp = format(new Date(timestamp), 'yyyy-MM-dd   HH:mm:ss  O')
        //console.log(`date: ${timestamp}`)
        this.state = {
            hookType: this.props.data.hookType,
            fhirVersion: this.props.data.fhirVersion,
            success: this.props.data.timeline ? this.props.data.timeline[4] : false,
            timeStamp: timestamp,
            viewDetails: false
        };

        this.openDetails = this.openDetails.bind(this);

    }


    openDetails() {

        this.setState((prevState) => {
            return { viewDetails: !prevState.viewDetails };
        })


    }
    render() {
        return (
            <div>

                <div
                    className={"requestEntry " +
                        [this.state.success ? "successRequest " : "failureRequest "] +
                        [this.state.viewDetails ? "active" : ""]
                    }
                    onClick={this.openDetails}>
                    <div className="element timestamp">
                        {this.state.timeStamp}
                    </div>
                    <div className="element fhirversion">
                        {this.state.fhirVersion}
                    </div>
                    <div className="element hooktype">
                        {this.state.hookType}
                    </div>
                    <div className="element successElement">
                        {this.state.success ? "success" : "failure"}
                    </div>
                </div>
                {this.state.viewDetails ? <DetailEntry data={this.props.data} /> : null}
            </div>
        )
    }
}