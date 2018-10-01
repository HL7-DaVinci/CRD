import React, {Component} from 'react';
import './request.css';
export default class RequestEntry extends Component {
    constructor(props){
        super(props);
        this.state={
            hookType:this.props.data.hookType,
            fhirVersion:this.props.data.fhirVersion,
            success: this.props.data.success,
            timeStamp: new Date(this.props.data.timeStamp)
        };
         
    }

    generateData(){
        console.log("henlo");
    }
     render() {
         return (
             <div>
                 <div className={"requestEntry " + [this.state.success?"successRequest":"failureRequest"]} onClick={this.generateData}>
                    <div className="element timestamp">
                        {this.state.timeStamp.toISOString()}
                    </div>
                    <div className="element fhirversion">
                        {this.state.fhirVersion}
                    </div>
                    <div className="element hooktype">
                        {this.state.hookType}
                    </div>
                    <div className="element successElement">
                        {this.state.success?"success":"failure"}
                    </div>
                 </div>
             </div>
         )
    }
} 