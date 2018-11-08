import React, {Component} from 'react';
import './detail.css';
import './request.css';

const codeSystemConversion = {
    "http://www.ama-assn.org/go/cpt":"CPT",
    "https://bluebutton.cms.gov/resources/codesystem/hcpcs":"HCPCS"
}
export default class HealthInfo extends Component {
    constructor(props){
        super(props);
        this.state={
            requestInfo: {}
        };
         
    }

    componentDidMount(){
        // temporarily hard code the data
        var dataProps = this.props.data;
        this.setState({requestInfo: {"age":dataProps.patientAge,"gender":dataProps.patientGender,"Patient State":dataProps.patientAddressState,"Provider State":dataProps.providerAddressState,"code":dataProps.code,"codeSystem":codeSystemConversion[dataProps.codeSystem]}})
    }

     render() {
         return (
             <div>
                <div className="healthInfoHeader">
                    Request Info
                </div>
                <div className="healthInfo">
                    {Object.keys(this.state.requestInfo).map(key=>{
                        return(  
                            <div key = {key} className="infoEntry">
                            <span className="infoTitle">{key}</span>: {this.state.requestInfo[key]}
                            </div>
                        )
                    })}
                </div>
             </div>

         )

    }
} 