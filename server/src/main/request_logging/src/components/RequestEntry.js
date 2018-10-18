import React, {Component} from 'react';
import DetailEntry from './DetailEntry';
import './request.css';

export default class RequestEntry extends Component {
    constructor(props){
        super(props);
        this.state={
            hookType:this.props.data.hookType,
            fhirVersion:this.props.data.fhirVersion,
            success: this.props.data.timeline?this.props.data.timeline[4]:false,
            timeStamp: new Date(this.props.data.timestamp),
            viewDetails: false
        };

        this.openDetails = this.openDetails.bind(this);
         
    }


    openDetails(){

        this.setState((prevState)=>{
            return {viewDetails: !prevState.viewDetails};
        })

        
    }
     render() {
         return (
             <div>

                 <div 
                 className={"requestEntry " + 
                 [this.state.success?"successRequest ":"failureRequest "] +
                 [this.state.viewDetails?"active":""]
                } 
                 onClick={this.openDetails}>
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
                 {this.state.viewDetails?<DetailEntry data={this.props.data} />:null}
             </div>
         )
    }
} 